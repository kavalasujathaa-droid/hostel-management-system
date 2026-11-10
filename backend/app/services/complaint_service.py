from typing import List, Optional
from fastapi import UploadFile
from sqlalchemy.orm import Session

from app.core.exceptions import NotFoundError, ValidationError
from app.models.models import Complaint, Room, TenantStay, User
from app.schemas.complaint import ComplaintCreate, ComplaintResponse, ComplaintUpdate
from app.services.audit_service import log_audit_event
from app.services.complaint_attachment_service import save_complaint_attachment
from app.services.notification_service import create_notification

ALLOWED_CATEGORIES = {"Plumbing", "Electrical", "Cleaning", "Maintenance", "Other"}


def create_complaint(
    db: Session,
    req: ComplaintCreate,
    tenant_user_id: int,
    photo_file: Optional[UploadFile] = None,
) -> ComplaintResponse:
    """Create a new complaint with title, category, description, and optional photo attachment."""
    stay_id = req.tenant_stay_id
    if stay_id is None:
        active_stay = (
            db.query(TenantStay)
            .filter(TenantStay.tenant_user_id == tenant_user_id, TenantStay.status == "ACTIVE")
            .first()
        )
        if not active_stay:
            raise ValidationError("You do not have an active stay to register a complaint against")
        stay_id = active_stay.id
    else:
        # Verify stay belongs to this tenant
        stay = db.query(TenantStay).filter(TenantStay.id == stay_id).first()
        if not stay or stay.tenant_user_id != tenant_user_id:
            raise ValidationError("Invalid tenant stay specified")

    category = req.category.strip()
    if category not in ALLOWED_CATEGORIES:
        category = "Other"

    attachment_path = None
    if photo_file is not None:
        attachment_path = save_complaint_attachment(photo_file)

    complaint = Complaint(
        tenant_stay_id=stay_id,
        title=req.title.strip(),
        category=category,
        description=req.description.strip(),
        status="OPEN",
        attachment_path=attachment_path,
    )
    db.add(complaint)
    db.flush()

    log_audit_event(
        db=db,
        action="COMPLAINT_CREATED",
        entity_type="COMPLAINT",
        entity_id=complaint.id,
        actor_user_id=tenant_user_id,
        details={
            "tenant_stay_id": stay_id,
            "title": complaint.title,
            "category": complaint.category,
            "has_attachment": bool(attachment_path),
        },
    )
    db.commit()
    db.refresh(complaint)

    return get_complaint_by_id(db, complaint.id)


def update_complaint_status(
    db: Session,
    complaint_id: int,
    req: ComplaintUpdate,
    owner_user_id: int,
) -> ComplaintResponse:
    """Update complaint status (OPEN, IN PROGRESS, RESOLVED)."""
    complaint = db.query(Complaint).filter(Complaint.id == complaint_id).with_for_update().first()
    if not complaint:
        raise NotFoundError(f"Complaint with id {complaint_id} not found")

    complaint.status = req.status
    log_audit_event(
        db=db,
        action="COMPLAINT_STATUS_UPDATED",
        entity_type="COMPLAINT",
        entity_id=complaint.id,
        actor_user_id=owner_user_id,
        details={"new_status": req.status},
    )

    # Notify tenant of progress or resolution
    stay = db.query(TenantStay).filter(TenantStay.id == complaint.tenant_stay_id).first()
    if stay:
        status_label = "resolved" if req.status == "RESOLVED" else req.status.lower()
        create_notification(
            db=db,
            user_id=stay.tenant_user_id,
            title="Complaint Status Updated",
            message=f"Your complaint '{complaint.title}' (#{complaint.id}) is now: {req.status}",
            notification_type="COMPLAINT_UPDATE",
            category="ALERT",
        )

    db.commit()
    return get_complaint_by_id(db, complaint.id)


def list_complaints(
    db: Session,
    tenant_user_id: Optional[int] = None,
    status: Optional[str] = None,
) -> List[ComplaintResponse]:
    """List complaints with tenant and room metadata."""
    query = (
        db.query(Complaint, TenantStay, User, Room)
        .join(TenantStay, Complaint.tenant_stay_id == TenantStay.id)
        .join(User, TenantStay.tenant_user_id == User.id)
        .join(Room, TenantStay.room_id == Room.id)
    )

    if tenant_user_id is not None:
        query = query.filter(TenantStay.tenant_user_id == tenant_user_id)
    if status is not None:
        query = query.filter(Complaint.status == status)

    rows = query.order_by(Complaint.created_at.desc()).all()
    return [
        ComplaintResponse(
            id=c.id,
            tenant_stay_id=c.tenant_stay_id,
            tenant_name=u.full_name,
            room_number=r.room_number,
            title=c.title or "General Issue",
            category=c.category or "Other",
            description=c.description,
            status=c.status,
            attachment_path=c.attachment_path,
            created_at=c.created_at,
            updated_at=c.updated_at,
        )
        for c, s, u, r in rows
    ]


def get_complaint_by_id(db: Session, complaint_id: int) -> ComplaintResponse:
    """Retrieve details for a single complaint."""
    row = (
        db.query(Complaint, TenantStay, User, Room)
        .join(TenantStay, Complaint.tenant_stay_id == TenantStay.id)
        .join(User, TenantStay.tenant_user_id == User.id)
        .join(Room, TenantStay.room_id == Room.id)
        .filter(Complaint.id == complaint_id)
        .first()
    )
    if not row:
        raise NotFoundError(f"Complaint with id {complaint_id} not found")

    c, s, u, r = row
    return ComplaintResponse(
        id=c.id,
        tenant_stay_id=c.tenant_stay_id,
        tenant_name=u.full_name,
        room_number=r.room_number,
        title=c.title or "General Issue",
        category=c.category or "Other",
        description=c.description,
        status=c.status,
        attachment_path=c.attachment_path,
        created_at=c.created_at,
        updated_at=c.updated_at,
    )
