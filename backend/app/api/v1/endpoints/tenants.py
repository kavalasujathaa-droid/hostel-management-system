from datetime import date
from decimal import Decimal
from typing import Any, Dict, List
from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.core.dependencies import require_owner
from app.core.exceptions import ConflictError, NotFoundError, ValidationError
from app.core.security import hash_password
from app.db.session import get_db
from app.models.models import TenantStay, User
from app.schemas.stay import TenantStayCreate
from app.schemas.user import TenantCreateRequest, UserResponse
from app.services import stay_service
from app.services.audit_service import log_audit_event

router = APIRouter()


@router.post("", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
def create_tenant(
    req: TenantCreateRequest,
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> UserResponse:
    """Create tenant account and optionally assign to an active room stay."""
    if db.query(User).filter(User.email == req.email).first():
        raise ConflictError(f"User with email '{req.email}' already exists")

    tenant = User(
        full_name=req.full_name,
        email=req.email,
        password_hash=hash_password(req.password),
        role="TENANT",
        phone=req.phone,
        is_active=True,
    )
    db.add(tenant)
    db.flush()

    log_audit_event(
        db=db,
        action="TENANT_ACCOUNT_CREATED",
        entity_type="USER",
        entity_id=tenant.id,
        actor_user_id=current_user.id,
        details={"email": tenant.email},
    )

    # Optional immediate room stay assignment
    if req.room_id is not None:
        if not req.joining_date or req.monthly_rent is None:
            raise ValidationError("joining_date and monthly_rent are required when assigning a room")
        try:
            j_date = date.fromisoformat(req.joining_date)
        except ValueError:
            raise ValidationError("Invalid joining_date format (YYYY-MM-DD required)")

        stay_create = TenantStayCreate(
            tenant_user_id=tenant.id,
            room_id=req.room_id,
            joining_date=j_date,
            monthly_rent=Decimal(str(req.monthly_rent)),
        )
        stay_service.assign_tenant_to_room(db, stay_create, actor_user_id=current_user.id)

    db.commit()
    db.refresh(tenant)
    return UserResponse.model_validate(tenant)


@router.get("", response_model=List[UserResponse], status_code=status.HTTP_200_OK)
def list_tenants(
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> List[UserResponse]:
    tenants = db.query(User).filter(User.role == "TENANT").order_by(User.full_name).all()
    return [UserResponse.model_validate(t) for t in tenants]


@router.get("/{tenant_id}", response_model=Dict[str, Any], status_code=status.HTTP_200_OK)
def get_tenant_details(
    tenant_id: int,
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> Dict[str, Any]:
    tenant = db.query(User).filter(User.id == tenant_id, User.role == "TENANT").first()
    if not tenant:
        raise NotFoundError(f"Tenant with id {tenant_id} not found")

    active_stay = stay_service.get_active_stay_by_tenant_id(db, tenant_id)
    history = stay_service.get_stay_history_by_tenant_id(db, tenant_id)

    return {
        "tenant": UserResponse.model_validate(tenant),
        "active_stay": active_stay,
        "stay_history": history,
    }
