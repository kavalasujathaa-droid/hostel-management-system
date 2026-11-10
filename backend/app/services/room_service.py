from typing import List, Optional
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.core.exceptions import ConflictError, NotFoundError, ValidationError
from app.models.models import Room, TenantStay, User
from app.schemas.room import OccupantInfo, RoomCreate, RoomResponse, RoomUpdate
from app.services.audit_service import log_audit_event


def get_room_active_occupancy_counts(db: Session) -> dict[int, int]:
    """Return a mapping of room_id to count of ACTIVE tenant stays."""
    rows = (
        db.query(TenantStay.room_id, func.count(TenantStay.id))
        .filter(TenantStay.status == "ACTIVE")
        .group_by(TenantStay.room_id)
        .all()
    )
    return {room_id: count for room_id, count in rows}


def list_rooms(
    db: Session,
    floor: Optional[str] = None,
    is_active: Optional[bool] = None,
) -> List[RoomResponse]:
    """List rooms with calculated occupancy numbers."""
    query = db.query(Room)
    if floor is not None:
        query = query.filter(Room.floor == floor)
    if is_active is not None:
        query = query.filter(Room.is_active == is_active)

    rooms = query.order_by(Room.room_number).all()
    occupancy_map = get_room_active_occupancy_counts(db)

    results = []
    for r in rooms:
        active_count = occupancy_map.get(r.id, 0)
        available = max(0, r.capacity - active_count)
        res = RoomResponse(
            id=r.id,
            room_number=r.room_number,
            floor=r.floor,
            capacity=r.capacity,
            is_active=r.is_active,
            created_at=r.created_at,
            active_occupants=active_count,
            available_beds=available,
        )
        results.append(res)
    return results


def get_room_by_id(db: Session, room_id: int) -> RoomResponse:
    """Get single room details with active occupants list."""
    room = db.query(Room).filter(Room.id == room_id).first()
    if not room:
        raise NotFoundError(f"Room with id {room_id} not found")

    occupant_stays = (
        db.query(TenantStay, User)
        .join(User, TenantStay.tenant_user_id == User.id)
        .filter(TenantStay.room_id == room.id, TenantStay.status == "ACTIVE")
        .all()
    )

    occupants = [
        OccupantInfo(
            tenant_id=user.id,
            full_name=user.full_name,
            email=user.email,
            joining_date=stay.joining_date.isoformat(),
            stay_id=stay.id,
        )
        for stay, user in occupant_stays
    ]

    active_count = len(occupants)
    available = max(0, room.capacity - active_count)

    return RoomResponse(
        id=room.id,
        room_number=room.room_number,
        floor=room.floor,
        capacity=room.capacity,
        is_active=room.is_active,
        created_at=room.created_at,
        active_occupants=active_count,
        available_beds=available,
        occupants=occupants,
    )


def create_room(db: Session, req: RoomCreate, actor_user_id: int) -> RoomResponse:
    """Create a new room with capacity check."""
    if db.query(Room).filter(Room.room_number == req.room_number).first():
        raise ConflictError(f"Room number '{req.room_number}' already exists")

    room = Room(
        room_number=req.room_number,
        floor=req.floor,
        capacity=req.capacity,
        is_active=True,
    )
    db.add(room)
    db.flush()

    log_audit_event(
        db=db,
        action="ROOM_CREATED",
        entity_type="ROOM",
        entity_id=room.id,
        actor_user_id=actor_user_id,
        details={"room_number": room.room_number, "capacity": room.capacity},
    )
    db.commit()
    db.refresh(room)

    return RoomResponse(
        id=room.id,
        room_number=room.room_number,
        floor=room.floor,
        capacity=room.capacity,
        is_active=room.is_active,
        created_at=room.created_at,
        active_occupants=0,
        available_beds=room.capacity,
    )


def update_room(db: Session, room_id: int, req: RoomUpdate, actor_user_id: int) -> RoomResponse:
    """Update room details ensuring capacity is not reduced below active occupants."""
    room = db.query(Room).filter(Room.id == room_id).with_for_update().first()
    if not room:
        raise NotFoundError(f"Room with id {room_id} not found")

    active_count = (
        db.query(func.count(TenantStay.id))
        .filter(TenantStay.room_id == room.id, TenantStay.status == "ACTIVE")
        .scalar()
        or 0
    )

    if req.capacity is not None:
        if req.capacity < active_count:
            raise ValidationError(
                f"Cannot reduce capacity to {req.capacity}: room currently has {active_count} active occupant(s)"
            )
        room.capacity = req.capacity

    if req.floor is not None:
        room.floor = req.floor

    if req.is_active is not None:
        if not req.is_active and active_count > 0:
            raise ConflictError(
                f"Cannot deactivate room '{room.room_number}' while it has {active_count} active occupant(s)"
            )
        room.is_active = req.is_active

    log_audit_event(
        db=db,
        action="ROOM_UPDATED",
        entity_type="ROOM",
        entity_id=room.id,
        actor_user_id=actor_user_id,
        details={"capacity": room.capacity, "floor": room.floor, "is_active": room.is_active},
    )
    db.commit()
    db.refresh(room)

    return RoomResponse(
        id=room.id,
        room_number=room.room_number,
        floor=room.floor,
        capacity=room.capacity,
        is_active=room.is_active,
        created_at=room.created_at,
        active_occupants=active_count,
        available_beds=max(0, room.capacity - active_count),
    )


def delete_room(db: Session, room_id: int, actor_user_id: int) -> None:
    """Soft-delete / deactivate a room."""
    room = db.query(Room).filter(Room.id == room_id).with_for_update().first()
    if not room:
        raise NotFoundError(f"Room with id {room_id} not found")

    active_count = (
        db.query(func.count(TenantStay.id))
        .filter(TenantStay.room_id == room.id, TenantStay.status == "ACTIVE")
        .scalar()
        or 0
    )
    if active_count > 0:
        raise ConflictError(
            f"Cannot deactivate room '{room.room_number}' while it has {active_count} active occupant(s)"
        )

    room.is_active = False
    log_audit_event(
        db=db,
        action="ROOM_DEACTIVATED",
        entity_type="ROOM",
        entity_id=room.id,
        actor_user_id=actor_user_id,
    )
    db.commit()
