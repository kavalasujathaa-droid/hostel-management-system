from datetime import date
from decimal import Decimal
from typing import List, Optional
from sqlalchemy import func
from sqlalchemy.exc import DBAPIError, IntegrityError, OperationalError
from sqlalchemy.orm import Session

from app.core.exceptions import ConflictError, NotFoundError, ValidationError
from app.models.models import Room, TenantStay, User
from app.schemas.stay import TenantStayCreate, TenantStayResponse
from app.services.audit_service import log_audit_event


def assign_tenant_to_room(
    db: Session,
    req: TenantStayCreate,
    actor_user_id: int,
) -> TenantStayResponse:
    """
    Assign a tenant to a room with strict row-locking to eliminate room oversubscription race conditions.
    """
    try:
        # Verify tenant exists and has TENANT role
        tenant = db.query(User).filter(User.id == req.tenant_user_id).first()
        if not tenant:
            raise NotFoundError(f"Tenant with id {req.tenant_user_id} not found")
        if tenant.role != "TENANT":
            raise ValidationError("Only users with TENANT role can be assigned to a room stay")
        if not tenant.is_active:
            raise ValidationError("Cannot assign inactive tenant to a room")

        # Check if tenant already has an active stay
        existing_active = (
            db.query(TenantStay)
            .filter(TenantStay.tenant_user_id == tenant.id, TenantStay.status == "ACTIVE")
            .first()
        )
        if existing_active:
            raise ConflictError(f"Tenant {tenant.full_name} already has an active stay (Stay ID: {existing_active.id})")

        # Atomically lock the room row and mark updated_at to ensure write-conflict detection
        room = db.query(Room).filter(Room.id == req.room_id).with_for_update().first()
        if not room:
            raise NotFoundError(f"Room with id {req.room_id} not found")
        if not room.is_active:
            raise ConflictError(f"Room {room.room_number} is deactivated and cannot accept new occupants")

        room.updated_at = func.now()
        db.flush()

        # Check room capacity
        current_occupants = (
            db.query(func.count(TenantStay.id))
            .filter(TenantStay.room_id == room.id, TenantStay.status == "ACTIVE")
            .scalar()
            or 0
        )

        if current_occupants >= room.capacity:
            raise ConflictError(
                f"Room {room.room_number} is already at full capacity ({current_occupants}/{room.capacity})"
            )

        stay = TenantStay(
            tenant_user_id=tenant.id,
            room_id=room.id,
            joining_date=req.joining_date,
            monthly_rent=req.monthly_rent,
            status="ACTIVE",
        )
        db.add(stay)
        db.flush()

        log_audit_event(
            db=db,
            action="TENANT_STAY_ASSIGNED",
            entity_type="TENANT_STAY",
            entity_id=stay.id,
            actor_user_id=actor_user_id,
            details={
                "tenant_id": tenant.id,
                "room_id": room.id,
                "room_number": room.room_number,
                "monthly_rent": str(req.monthly_rent),
            },
        )
        db.commit()
        db.refresh(stay)

        return TenantStayResponse(
            id=stay.id,
            tenant_user_id=stay.tenant_user_id,
            room_id=stay.room_id,
            room_number=room.room_number,
            joining_date=stay.joining_date,
            approved_vacating_date=stay.approved_vacating_date,
            monthly_rent=stay.monthly_rent,
            status=stay.status,
            created_at=stay.created_at,
        )
    except (OperationalError, IntegrityError, DBAPIError) as db_err:
        db.rollback()
        raise ConflictError(f"Concurrent room assignment lock conflict on room {req.room_id}: {db_err}")


def get_active_stay_by_tenant_id(db: Session, tenant_id: int) -> Optional[TenantStayResponse]:
    """Retrieve active stay for a specific tenant."""
    row = (
        db.query(TenantStay, Room)
        .join(Room, TenantStay.room_id == Room.id)
        .filter(TenantStay.tenant_user_id == tenant_id, TenantStay.status == "ACTIVE")
        .first()
    )
    if not row:
        return None

    stay, room = row
    return TenantStayResponse(
        id=stay.id,
        tenant_user_id=stay.tenant_user_id,
        room_id=stay.room_id,
        room_number=room.room_number,
        joining_date=stay.joining_date,
        approved_vacating_date=stay.approved_vacating_date,
        monthly_rent=stay.monthly_rent,
        status=stay.status,
        created_at=stay.created_at,
    )


def get_stay_history_by_tenant_id(db: Session, tenant_id: int) -> List[TenantStayResponse]:
    """Retrieve all stays (active and completed) for a tenant."""
    rows = (
        db.query(TenantStay, Room)
        .join(Room, TenantStay.room_id == Room.id)
        .filter(TenantStay.tenant_user_id == tenant_id)
        .order_by(TenantStay.joining_date.desc())
        .all()
    )
    return [
        TenantStayResponse(
            id=stay.id,
            tenant_user_id=stay.tenant_user_id,
            room_id=stay.room_id,
            room_number=room.room_number,
            joining_date=stay.joining_date,
            approved_vacating_date=stay.approved_vacating_date,
            monthly_rent=stay.monthly_rent,
            status=stay.status,
            created_at=stay.created_at,
        )
        for stay, room in rows
    ]
