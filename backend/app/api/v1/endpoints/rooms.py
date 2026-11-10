from typing import List, Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, require_owner
from app.db.session import get_db
from app.models.models import User
from app.schemas.room import RoomCreate, RoomResponse, RoomUpdate
from app.services import room_service

router = APIRouter()


@router.get("", response_model=List[RoomResponse], status_code=status.HTTP_200_OK)
def list_all_rooms(
    floor: Optional[str] = Query(None),
    is_active: Optional[bool] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> List[RoomResponse]:
    return room_service.list_rooms(db, floor=floor, is_active=is_active)


@router.post("", response_model=RoomResponse, status_code=status.HTTP_201_CREATED)
def create_new_room(
    req: RoomCreate,
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> RoomResponse:
    return room_service.create_room(db, req, actor_user_id=current_user.id)


@router.get("/{room_id}", response_model=RoomResponse, status_code=status.HTTP_200_OK)
def get_room(
    room_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> RoomResponse:
    return room_service.get_room_by_id(db, room_id)


@router.put("/{room_id}", response_model=RoomResponse, status_code=status.HTTP_200_OK)
def update_room(
    room_id: int,
    req: RoomUpdate,
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> RoomResponse:
    return room_service.update_room(db, room_id, req, actor_user_id=current_user.id)


@router.delete("/{room_id}", status_code=status.HTTP_200_OK)
def delete_room(
    room_id: int,
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
):
    room_service.delete_room(db, room_id, actor_user_id=current_user.id)
    return {"success": True, "message": f"Room {room_id} deactivated successfully"}
