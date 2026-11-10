import os
import uuid
from typing import List, Optional
from fastapi import UploadFile
from sqlalchemy.orm import Session

from app.core.config import BACKEND_DIR
from app.core.exceptions import NotFoundError, ValidationError
from app.models.models import PaymentQR
from app.schemas.payment_qr import PaymentQRResponse
from app.services.audit_service import log_audit_event

# Directory for storing uploaded UPI QR images safely
UPLOAD_DIR = BACKEND_DIR / "uploads" / "qr"
ALLOWED_EXTENSIONS = {".png", ".jpg", ".jpeg", ".webp"}


def save_qr_file(file: UploadFile) -> str:
    """Validate and safely save QR image to durable storage, returning relative path."""
    UPLOAD_DIR.mkdir(parents=True, exist_ok=True)

    filename = file.filename or "qr.png"
    ext = os.path.splitext(filename)[1].lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise ValidationError(f"Invalid image extension '{ext}'. Allowed: {', '.join(ALLOWED_EXTENSIONS)}")

    unique_filename = f"qr_{uuid.uuid4().hex[:12]}{ext}"
    destination_path = UPLOAD_DIR / unique_filename

    content = file.file.read()
    if len(content) == 0:
        raise ValidationError("Uploaded file is empty")
    if len(content) > 5 * 1024 * 1024:  # 5 MB max
        raise ValidationError("Uploaded image exceeds maximum allowed size of 5 MB")

    with open(destination_path, "wb") as f:
        f.write(content)

    relative_path = f"uploads/qr/{unique_filename}"
    return relative_path


def upload_and_activate_qr(db: Session, file: UploadFile, owner_user_id: int) -> PaymentQRResponse:
    """Upload a new UPI QR code and set as currently active QR."""
    relative_path = save_qr_file(file)

    # Deactivate existing QRs to enforce single active QR
    db.query(PaymentQR).filter(PaymentQR.is_active == True).update({"is_active": False})  # noqa: E712

    qr = PaymentQR(
        uploaded_by=owner_user_id,
        image_path=relative_path,
        is_active=True,
    )
    db.add(qr)
    db.flush()

    log_audit_event(
        db=db,
        action="PAYMENT_QR_UPLOADED",
        entity_type="PAYMENT_QR",
        entity_id=qr.id,
        actor_user_id=owner_user_id,
        details={"path": relative_path},
    )
    db.commit()
    db.refresh(qr)

    return PaymentQRResponse.model_validate(qr)


def get_active_qr(db: Session) -> Optional[PaymentQRResponse]:
    """Retrieve currently active payment QR code."""
    qr = db.query(PaymentQR).filter(PaymentQR.is_active == True).order_by(PaymentQR.created_at.desc()).first()  # noqa: E712
    if not qr:
        return None
    return PaymentQRResponse.model_validate(qr)


def list_all_qrs(db: Session) -> List[PaymentQRResponse]:
    """List all uploaded payment QR codes."""
    rows = db.query(PaymentQR).order_by(PaymentQR.created_at.desc()).all()
    return [PaymentQRResponse.model_validate(q) for q in rows]
