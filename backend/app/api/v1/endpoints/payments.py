from decimal import Decimal
import mimetypes
from typing import List, Optional
from fastapi import APIRouter, Depends, Query, Request, UploadFile, status
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, require_owner, require_tenant
from app.core.exceptions import ForbiddenError, NotFoundError, ValidationError
from app.db.session import get_db
from app.models.models import Payment, User
from app.schemas.payment import PaymentResponse, PaymentReviewRequest, PaymentSubmissionRequest
from app.services import payment_proof_service, payment_service

router = APIRouter()


@router.post("", response_model=PaymentResponse, status_code=status.HTTP_201_CREATED)
async def submit_payment(
    request: Request,
    current_user: User = Depends(require_tenant),
    db: Session = Depends(get_db),
) -> PaymentResponse:
    """
    Submit a rent payment with unique UTR and optional payment screenshot.
    Supports both application/json and multipart/form-data.
    """
    content_type = request.headers.get("content-type", "")
    proof_file: Optional[UploadFile] = None

    if "multipart/form-data" in content_type:
        form = await request.form()
        try:
            raw_rent_charge_id = form.get("rent_charge_id")
            raw_amount = form.get("amount")
            raw_utr = form.get("utr")
            if not raw_rent_charge_id or not raw_amount or not raw_utr:
                raise ValidationError("rent_charge_id, amount, and utr are required fields")

            rent_charge_id = int(str(raw_rent_charge_id))
            amount = Decimal(str(raw_amount))
            utr = str(raw_utr).strip()

            raw_file = form.get("proof_file")
            if raw_file and hasattr(raw_file, "filename") and getattr(raw_file, "filename"):
                proof_file = raw_file

            req = PaymentSubmissionRequest(
                rent_charge_id=rent_charge_id,
                amount=amount,
                utr=utr,
            )
        except (ValueError, TypeError) as exc:
            raise ValidationError(f"Invalid form data: {str(exc)}")
    else:
        body = await request.json()
        req = PaymentSubmissionRequest(**body)

    return payment_service.submit_payment(
        db=db,
        req=req,
        tenant_user_id=current_user.id,
        proof_file=proof_file,
    )


@router.get("", response_model=List[PaymentResponse], status_code=status.HTTP_200_OK)
def list_payments(
    tenant_id: Optional[int] = Query(None),
    status: Optional[str] = Query(None),
    rent_charge_id: Optional[int] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> List[PaymentResponse]:
    effective_tenant_id = current_user.id if current_user.role == "TENANT" else tenant_id
    return payment_service.list_payments(
        db=db,
        tenant_user_id=effective_tenant_id,
        status=status,
        rent_charge_id=rent_charge_id,
    )


@router.get("/{payment_id}/proof", status_code=status.HTTP_200_OK)
def get_payment_proof(
    payment_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """
    Secure authenticated retrieval of payment proof image.
    Accessible ONLY by the submitting tenant or the owner.
    Never exposed to unauthenticated users.
    """
    payment = db.query(Payment).filter(Payment.id == payment_id).first()
    if not payment or not payment.proof_image_path:
        raise NotFoundError("Payment proof image not found")

    # Authorization check
    if current_user.role != "OWNER" and current_user.id != payment.submitted_by:
        raise ForbiddenError("You are not authorized to view this payment proof")

    full_path = payment_proof_service.get_proof_full_path(payment.proof_image_path)
    if not full_path.is_file():
        raise NotFoundError("Payment proof file not found in storage")

    content_type, _ = mimetypes.guess_type(str(full_path))
    media_type = content_type or "application/octet-stream"

    return FileResponse(
        path=str(full_path),
        media_type=media_type,
        filename=full_path.name,
    )


@router.put("/{payment_id}/approve", response_model=PaymentResponse, status_code=status.HTTP_200_OK)
def approve_payment(
    payment_id: int,
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> PaymentResponse:
    """Atomically approve a payment, updating rent charge amount_paid and status."""
    return payment_service.review_payment(
        db=db,
        payment_id=payment_id,
        action="APPROVE",
        review_req=None,
        owner_user_id=current_user.id,
    )


@router.put("/{payment_id}/reject", response_model=PaymentResponse, status_code=status.HTTP_200_OK)
def reject_payment(
    payment_id: int,
    req: PaymentReviewRequest,
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> PaymentResponse:
    """Reject a payment with reason; rent balances remain unaffected."""
    return payment_service.review_payment(
        db=db,
        payment_id=payment_id,
        action="REJECT",
        review_req=req,
        owner_user_id=current_user.id,
    )
