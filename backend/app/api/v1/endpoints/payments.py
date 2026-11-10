from typing import List, Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, require_owner, require_tenant
from app.db.session import get_db
from app.models.models import User
from app.schemas.payment import PaymentResponse, PaymentReviewRequest, PaymentSubmissionRequest
from app.services import payment_service

router = APIRouter()


@router.post("", response_model=PaymentResponse, status_code=status.HTTP_201_CREATED)
def submit_payment(
    req: PaymentSubmissionRequest,
    current_user: User = Depends(require_tenant),
    db: Session = Depends(get_db),
) -> PaymentResponse:
    """Submit a rent payment with unique UTR."""
    return payment_service.submit_payment(db, req, tenant_user_id=current_user.id)


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
