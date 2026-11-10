from typing import List, Optional
from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, require_owner, require_tenant
from app.db.session import get_db
from app.models.models import User
from app.schemas.stay import TenantStayCreate, TenantStayResponse
from app.services import stay_service

router = APIRouter()


@router.post("", response_model=TenantStayResponse, status_code=status.HTTP_201_CREATED)
def assign_room_stay(
    req: TenantStayCreate,
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> TenantStayResponse:
    """Assign tenant to room with atomic capacity check."""
    return stay_service.assign_tenant_to_room(db, req, actor_user_id=current_user.id)


@router.get("/active", response_model=Optional[TenantStayResponse], status_code=status.HTTP_200_OK)
def get_my_active_stay(
    current_user: User = Depends(require_tenant),
    db: Session = Depends(get_db),
) -> Optional[TenantStayResponse]:
    return stay_service.get_active_stay_by_tenant_id(db, current_user.id)


@router.get("/history", response_model=List[TenantStayResponse], status_code=status.HTTP_200_OK)
def get_my_stay_history(
    current_user: User = Depends(require_tenant),
    db: Session = Depends(get_db),
) -> List[TenantStayResponse]:
    return stay_service.get_stay_history_by_tenant_id(db, current_user.id)
