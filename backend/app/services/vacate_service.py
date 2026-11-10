from datetime import datetime
from typing import List, Optional
from sqlalchemy.orm import Session

from app.core.exceptions import ConflictError, NotFoundError, ValidationError
from app.models.models import Room, TenantStay, User, VacateRequest
from app.schemas.vacate import VacateRequestCreate, VacateRequestResponse, VacateRequestReview
from app.services.audit_service import log_audit_event
from app.services.notification_service import create_notification


def submit_vacate_request(
    db: Session,
    req: VacateRequestCreate,
    tenant_user_id: int,
) -> VacateRequestResponse:
    """Submit a request to vacate an active stay."""
    stay_id = req.tenant_stay_id
    if stay_id is None:
        active_stay = (
            db.query(TenantStay)
            .filter(TenantStay.tenant_user_id == tenant_user_id, TenantStay.status == "ACTIVE")
            .first()
        )
        if not active_stay:
            raise ValidationError("You do not have an active stay to submit a vacate request for")
        stay = active_stay
    else:
        stay = db.query(TenantStay).filter(TenantStay.id == stay_id).first()
        if not stay or stay.tenant_user_id != tenant_user_id:
            raise ValidationError("Invalid tenant stay specified")

    # Enforce ck_stays_vacating_date: requested date must be >= joining_date
    if req.requested_vacating_date < stay.joining_date:
        raise ValidationError(
            f"Requested vacating date ({req.requested_vacating_date}) cannot be earlier than joining date ({stay.joining_date})"
        )

    # Check for existing pending request
    pending = (
        db.query(VacateRequest)
        .filter(VacateRequest.tenant_stay_id == stay.id, VacateRequest.status == "PENDING")
        .first()
    )
    if pending:
        raise ConflictError("A pending vacate request is already awaiting owner review")

    vacate_req = VacateRequest(
        tenant_stay_id=stay.id,
        requested_vacating_date=req.requested_vacating_date,
        status="PENDING",
    )
    db.add(vacate_req)
    db.flush()

    log_audit_event(
        db=db,
        action="VACATE_REQUEST_SUBMITTED",
        entity_type="VACATE_REQUEST",
        entity_id=vacate_req.id,
        actor_user_id=tenant_user_id,
        details={
            "tenant_stay_id": stay.id,
            "requested_date": req.requested_vacating_date.isoformat(),
        },
    )
    db.commit()
    db.refresh(vacate_req)

    return get_vacate_request_by_id(db, vacate_req.id)


def review_vacate_request(
    db: Session,
    request_id: int,
    req: VacateRequestReview,
    owner_user_id: int,
) -> VacateRequestResponse:
    """Review and approve or reject a vacate request."""
    vacate_req = db.query(VacateRequest).filter(VacateRequest.id == request_id).with_for_update().first()
    if not vacate_req:
        raise NotFoundError(f"Vacate request with id {request_id} not found")

    if vacate_req.status != "PENDING":
        raise ConflictError(f"Vacate request has already been processed with status: {vacate_req.status}")

    stay = db.query(TenantStay).filter(TenantStay.id == vacate_req.tenant_stay_id).with_for_update().first()
    if not stay:
        raise NotFoundError(f"Associated stay with id {vacate_req.tenant_stay_id} not found")

    now = datetime.utcnow()

    if req.action == "APPROVE":
        vacate_req.status = "APPROVED"
        vacate_req.reviewed_by = owner_user_id
        vacate_req.reviewed_at = now
        stay.approved_vacating_date = vacate_req.requested_vacating_date

        create_notification(
            db=db,
            user_id=stay.tenant_user_id,
            title="Vacate Request Approved",
            message=f"Your request to vacate on {vacate_req.requested_vacating_date} has been approved.",
            notification_type="VACATE_APPROVED",
        )
    elif req.action == "REJECT":
        vacate_req.status = "REJECTED"
        vacate_req.rejection_reason = req.rejection_reason or "Declined by management"
        vacate_req.reviewed_by = owner_user_id
        vacate_req.reviewed_at = now

        create_notification(
            db=db,
            user_id=stay.tenant_user_id,
            title="Vacate Request Rejected",
            message=f"Your vacate request was rejected: {vacate_req.rejection_reason}",
            notification_type="VACATE_REJECTED",
        )
    else:
        raise ValidationError("Action must be 'APPROVE' or 'REJECT'")

    log_audit_event(
        db=db,
        action=f"VACATE_REQUEST_{req.action}D",
        entity_type="VACATE_REQUEST",
        entity_id=vacate_req.id,
        actor_user_id=owner_user_id,
        details={"action": req.action, "reason": vacate_req.rejection_reason},
    )
    db.commit()

    return get_vacate_request_by_id(db, vacate_req.id)


def list_vacate_requests(
    db: Session,
    tenant_user_id: Optional[int] = None,
    status: Optional[str] = None,
) -> List[VacateRequestResponse]:
    """List vacate requests with metadata."""
    query = (
        db.query(VacateRequest, TenantStay, User, Room)
        .join(TenantStay, VacateRequest.tenant_stay_id == TenantStay.id)
        .join(User, TenantStay.tenant_user_id == User.id)
        .join(Room, TenantStay.room_id == Room.id)
    )

    if tenant_user_id is not None:
        query = query.filter(TenantStay.tenant_user_id == tenant_user_id)
    if status is not None:
        query = query.filter(VacateRequest.status == status)

    rows = query.order_by(VacateRequest.requested_at.desc()).all()
    return [
        VacateRequestResponse(
            id=v.id,
            tenant_stay_id=v.tenant_stay_id,
            tenant_name=u.full_name,
            room_number=r.room_number,
            requested_vacating_date=v.requested_vacating_date,
            status=v.status,
            reviewed_by=v.reviewed_by,
            rejection_reason=v.rejection_reason,
            requested_at=v.requested_at,
            reviewed_at=v.reviewed_at,
        )
        for v, s, u, r in rows
    ]


def get_vacate_request_by_id(db: Session, request_id: int) -> VacateRequestResponse:
    """Retrieve details for a single vacate request."""
    row = (
        db.query(VacateRequest, TenantStay, User, Room)
        .join(TenantStay, VacateRequest.tenant_stay_id == TenantStay.id)
        .join(User, TenantStay.tenant_user_id == User.id)
        .join(Room, TenantStay.room_id == Room.id)
        .filter(VacateRequest.id == request_id)
        .first()
    )
    if not row:
        raise NotFoundError(f"Vacate request with id {request_id} not found")

    v, s, u, r = row
    return VacateRequestResponse(
        id=v.id,
        tenant_stay_id=v.tenant_stay_id,
        tenant_name=u.full_name,
        room_number=r.room_number,
        requested_vacating_date=v.requested_vacating_date,
        status=v.status,
        reviewed_by=v.reviewed_by,
        rejection_reason=v.rejection_reason,
        requested_at=v.requested_at,
        reviewed_at=v.reviewed_at,
    )
