from datetime import datetime
from decimal import Decimal
from typing import List, Optional
from fastapi import UploadFile
from sqlalchemy.orm import Session

from app.core.exceptions import ConflictError, NotFoundError, ValidationError
from app.models.models import Payment, RentCharge, TenantStay, User
from app.schemas.payment import PaymentResponse, PaymentReviewRequest, PaymentSubmissionRequest
from app.services.audit_service import log_audit_event
from app.services.notification_service import create_notification
from app.services.payment_proof_service import save_payment_proof


def submit_payment(
    db: Session,
    req: PaymentSubmissionRequest,
    tenant_user_id: int,
    proof_file: Optional[UploadFile] = None,
) -> PaymentResponse:
    """
    Submit a rent payment with UTR and optional proof image.
    Validates UTR uniqueness, verifies rent charge belongs to tenant's stay, and prevents overpayment.
    """
    # Verify unique UTR
    existing_utr = db.query(Payment).filter(Payment.utr == req.utr).first()
    if existing_utr:
        raise ConflictError(f"A payment with UTR '{req.utr}' has already been submitted")

    # Fetch and lock rent charge
    rent_charge = db.query(RentCharge).filter(RentCharge.id == req.rent_charge_id).with_for_update().first()
    if not rent_charge:
        raise NotFoundError(f"Rent charge with id {req.rent_charge_id} not found")

    # Verify that this rent charge belongs to the submitting tenant
    stay = db.query(TenantStay).filter(TenantStay.id == rent_charge.tenant_stay_id).first()
    if not stay or stay.tenant_user_id != tenant_user_id:
        raise ValidationError("You can only submit payments against your own rent charges")

    remaining_due = rent_charge.amount_due - rent_charge.amount_paid
    if remaining_due <= Decimal("0.00"):
        raise ValidationError("This rent charge has already been fully paid")

    if req.amount > remaining_due:
        raise ValidationError(
            f"Payment amount of ₹{req.amount} exceeds the remaining balance of ₹{remaining_due}"
        )

    proof_path = None
    if proof_file is not None:
        proof_path = save_payment_proof(proof_file)

    payment = Payment(
        rent_charge_id=rent_charge.id,
        submitted_by=tenant_user_id,
        amount=req.amount,
        utr=req.utr,
        status="PENDING",
        proof_image_path=proof_path,
    )
    db.add(payment)
    db.flush()

    log_audit_event(
        db=db,
        action="PAYMENT_SUBMITTED",
        entity_type="PAYMENT",
        entity_id=payment.id,
        actor_user_id=tenant_user_id,
        details={
            "rent_charge_id": rent_charge.id,
            "amount": str(req.amount),
            "utr": req.utr,
            "has_proof": bool(proof_path),
        },
    )
    db.commit()
    db.refresh(payment)

    user = db.query(User).filter(User.id == tenant_user_id).first()
    return PaymentResponse(
        id=payment.id,
        rent_charge_id=payment.rent_charge_id,
        submitted_by=payment.submitted_by,
        submitted_by_name=user.full_name if user else None,
        reviewed_by=payment.reviewed_by,
        amount=payment.amount,
        utr=payment.utr,
        status=payment.status,
        rejection_reason=payment.rejection_reason,
        proof_image_path=payment.proof_image_path,
        submitted_at=payment.submitted_at,
        reviewed_at=payment.reviewed_at,
    )


def review_payment(
    db: Session,
    payment_id: int,
    action: str,  # 'APPROVE' or 'REJECT'
    review_req: Optional[PaymentReviewRequest],
    owner_user_id: int,
) -> PaymentResponse:
    """
    Atomically verify and approve or reject a payment:
    - Row-locks payment and rent charge.
    - If APPROVED: updates rent_charge.amount_paid and status atomically.
    - If REJECTED: sets rejection reason, balance remains unaffected.
    - Enforces ck_rent_no_overpayment constraint.
    """
    payment = db.query(Payment).filter(Payment.id == payment_id).with_for_update().first()
    if not payment:
        raise NotFoundError(f"Payment with id {payment_id} not found")

    if payment.status != "PENDING":
        raise ConflictError(f"Payment has already been processed with status: {payment.status}")

    rent_charge = db.query(RentCharge).filter(RentCharge.id == payment.rent_charge_id).with_for_update().first()
    if not rent_charge:
        raise NotFoundError(f"Associated rent charge {payment.rent_charge_id} not found")

    now = datetime.utcnow()

    if action.upper() == "APPROVE":
        new_paid = rent_charge.amount_paid + payment.amount
        if new_paid > rent_charge.amount_due:
            raise ValidationError(
                f"Approving this payment would cause overpayment: ₹{new_paid} > ₹{rent_charge.amount_due}"
            )

        rent_charge.amount_paid = new_paid
        rent_charge.status = "PAID" if new_paid == rent_charge.amount_due else "PARTIALLY_PAID"

        payment.status = "APPROVED"
        payment.reviewed_by = owner_user_id
        payment.reviewed_at = now

        # In-app notification to tenant
        create_notification(
            db=db,
            user_id=payment.submitted_by,
            title="Payment Approved",
            message=f"Your payment of ₹{payment.amount} (UTR: {payment.utr}) has been verified and approved.",
            notification_type="PAYMENT_APPROVED",
        )
    elif action.upper() == "REJECT":
        payment.status = "REJECTED"
        payment.rejection_reason = (review_req.rejection_reason if review_req else None) or "Verification failed"
        payment.reviewed_by = owner_user_id
        payment.reviewed_at = now

        create_notification(
            db=db,
            user_id=payment.submitted_by,
            title="Payment Rejected",
            message=f"Your payment of ₹{payment.amount} (UTR: {payment.utr}) was rejected: {payment.rejection_reason}",
            notification_type="PAYMENT_REJECTED",
        )
    else:
        raise ValidationError("Action must be either 'APPROVE' or 'REJECT'")

    log_audit_event(
        db=db,
        action=f"PAYMENT_{action.upper()}D",
        entity_type="PAYMENT",
        entity_id=payment.id,
        actor_user_id=owner_user_id,
        details={
            "rent_charge_id": rent_charge.id,
            "amount": str(payment.amount),
            "action": action,
            "new_balance": str(rent_charge.amount_due - rent_charge.amount_paid),
        },
    )
    db.commit()
    db.refresh(payment)

    user = db.query(User).filter(User.id == payment.submitted_by).first()
    return PaymentResponse(
        id=payment.id,
        rent_charge_id=payment.rent_charge_id,
        submitted_by=payment.submitted_by,
        submitted_by_name=user.full_name if user else None,
        reviewed_by=payment.reviewed_by,
        amount=payment.amount,
        utr=payment.utr,
        status=payment.status,
        rejection_reason=payment.rejection_reason,
        proof_image_path=payment.proof_image_path,
        submitted_at=payment.submitted_at,
        reviewed_at=payment.reviewed_at,
    )


def list_payments(
    db: Session,
    tenant_user_id: Optional[int] = None,
    status: Optional[str] = None,
    rent_charge_id: Optional[int] = None,
) -> List[PaymentResponse]:
    """Query payments with user metadata."""
    query = db.query(Payment, User).join(User, Payment.submitted_by == User.id)

    if tenant_user_id is not None:
        query = query.filter(Payment.submitted_by == tenant_user_id)
    if status is not None:
        query = query.filter(Payment.status == status)
    if rent_charge_id is not None:
        query = query.filter(Payment.rent_charge_id == rent_charge_id)

    rows = query.order_by(Payment.submitted_at.desc()).all()
    return [
        PaymentResponse(
            id=p.id,
            rent_charge_id=p.rent_charge_id,
            submitted_by=p.submitted_by,
            submitted_by_name=u.full_name,
            reviewed_by=p.reviewed_by,
            amount=p.amount,
            utr=p.utr,
            status=p.status,
            rejection_reason=p.rejection_reason,
            proof_image_path=p.proof_image_path,
            submitted_at=p.submitted_at,
            reviewed_at=p.reviewed_at,
        )
        for p, u in rows
    ]
