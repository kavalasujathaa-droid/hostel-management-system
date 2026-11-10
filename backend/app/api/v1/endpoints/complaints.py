from typing import List, Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, require_owner, require_tenant
from app.db.session import get_db
from app.models.models import User
from app.schemas.complaint import ComplaintCreate, ComplaintResponse, ComplaintUpdate
from app.services import complaint_service

router = APIRouter()


@router.post("", response_model=ComplaintResponse, status_code=status.HTTP_201_CREATED)
def create_complaint(
    req: ComplaintCreate,
    current_user: User = Depends(require_tenant),
    db: Session = Depends(get_db),
) -> ComplaintResponse:
    """Submit a text complaint against tenant's active stay."""
    return complaint_service.create_complaint(db, req, tenant_user_id=current_user.id)


@router.get("", response_model=List[ComplaintResponse], status_code=status.HTTP_200_OK)
def list_complaints(
    status: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> List[ComplaintResponse]:
    """List complaints. Tenants only see their own complaints."""
    tenant_id = current_user.id if current_user.role == "TENANT" else None
    return complaint_service.list_complaints(db, tenant_user_id=tenant_id, status=status)


@router.get("/{complaint_id}", response_model=ComplaintResponse, status_code=status.HTTP_200_OK)
def get_complaint(
    complaint_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> ComplaintResponse:
    complaint = complaint_service.get_complaint_by_id(db, complaint_id)
    if current_user.role == "TENANT":
        user_complaints = complaint_service.list_complaints(db, tenant_user_id=current_user.id)
        if not any(c.id == complaint.id for c in user_complaints):
            from app.core.exceptions import ForbiddenError
            raise ForbiddenError("Access forbidden to this complaint")
    return complaint


@router.put("/{complaint_id}", response_model=ComplaintResponse, status_code=status.HTTP_200_OK)
def update_complaint_status(
    complaint_id: int,
    req: ComplaintUpdate,
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> ComplaintResponse:
    """Update complaint status (e.g. IN PROGRESS)."""
    return complaint_service.update_complaint_status(db, complaint_id, req, owner_user_id=current_user.id)
