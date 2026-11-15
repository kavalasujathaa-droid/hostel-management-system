from typing import List
from fastapi import APIRouter, Depends, status
from sqlalchemy.exc import DBAPIError, IntegrityError
from sqlalchemy.orm import Session

from app.core.dependencies import require_owner
from app.core.exceptions import ConflictError
from app.core.security import hash_password
from app.db.session import get_db
from app.models.models import User
from app.schemas.user import OwnerCreateRequest, UserResponse
from app.services.audit_service import log_audit_event

router = APIRouter()


@router.post("", response_model=UserResponse, status_code=status.HTTP_201_CREATED, summary="Create a new owner account")
def create_owner(
    req: OwnerCreateRequest,
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> UserResponse:
    """Allow an authenticated active OWNER to create another OWNER."""
    existing_user = db.query(User).filter(User.email == req.email.lower()).first()
    if existing_user:
        raise ConflictError(f"User with email '{req.email}' already exists")

    new_owner = User(
        full_name=req.full_name,
        email=req.email.lower(),
        password_hash=hash_password(req.password),
        role="OWNER",
        phone=req.phone,
        is_active=True,
    )
    db.add(new_owner)
    try:
        db.flush()
        log_audit_event(
            db=db,
            action="OWNER_ACCOUNT_CREATED",
            entity_type="USER",
            entity_id=new_owner.id,
            actor_user_id=current_user.id,
            details={"email": new_owner.email, "full_name": new_owner.full_name},
        )
        db.commit()
        db.refresh(new_owner)
    except (IntegrityError, DBAPIError):
        db.rollback()
        raise ConflictError(f"User with email '{req.email}' already exists")

    return UserResponse.model_validate(new_owner)


@router.get("", response_model=List[UserResponse], status_code=status.HTTP_200_OK, summary="List all owner accounts")
def list_owners(
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> List[UserResponse]:
    """Allow authenticated active OWNERs to list Owner accounts."""
    owners = (
        db.query(User)
        .filter(User.role == "OWNER")
        .order_by(User.created_at.asc())
        .all()
    )
    return [UserResponse.model_validate(owner) for owner in owners]
