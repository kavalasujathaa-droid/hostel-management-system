from typing import List, Optional
from fastapi import APIRouter, Depends, File, UploadFile, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, require_owner
from app.core.exceptions import NotFoundError
from app.db.session import get_db
from app.models.models import User
from app.schemas.payment_qr import PaymentQRResponse
from app.services import payment_qr_service

router = APIRouter()


@router.get("/active", response_model=PaymentQRResponse, status_code=status.HTTP_200_OK)
def get_active_payment_qr(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> PaymentQRResponse:
    """Get the currently active UPI QR code for tenant payments."""
    qr = payment_qr_service.get_active_qr(db)
    if not qr:
        raise NotFoundError("No active payment QR code is currently configured")
    return qr


@router.post("", response_model=PaymentQRResponse, status_code=status.HTTP_201_CREATED)
def upload_payment_qr(
    file: UploadFile = File(...),
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> PaymentQRResponse:
    """Upload and activate a new UPI QR code."""
    return payment_qr_service.upload_and_activate_qr(db, file, owner_user_id=current_user.id)


@router.get("", response_model=List[PaymentQRResponse], status_code=status.HTTP_200_OK)
def list_payment_qrs(
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> List[PaymentQRResponse]:
    return payment_qr_service.list_all_qrs(db)
