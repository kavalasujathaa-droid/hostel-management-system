from typing import List, Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, require_owner, require_tenant
from app.db.session import get_db
from app.models.models import User
from app.schemas.vacate import VacateRequestCreate, VacateRequestResponse, VacateRequestReview
from app.services import vacate_service

router = APIRouter()


@router.post("", response_model=VacateRequestResponse, status_code=status.HTTP_201_CREATED)
def submit_vacate_request(
    req: VacateRequestCreate,
    current_user: User = Depends(require_tenant),
    db: Session = Depends(get_db),
) -> VacateRequestResponse:
    """Submit a vacate request for tenant's active stay."""
    return vacate_service.submit_vacate_request(db, req, tenant_user_id=current_user.id)


@router.get("", response_model=List[VacateRequestResponse], status_code=status.HTTP_200_OK)
def list_vacate_requests(
    status: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> List[VacateRequestResponse]:
    """List vacate requests. Tenants only see their own requests."""
    tenant_id = current_user.id if current_user.role == "TENANT" else None
    return vacate_service.list_vacate_requests(db, tenant_user_id=tenant_id, status=status)


@router.put("/{request_id}/review", response_model=VacateRequestResponse, status_code=status.HTTP_200_OK)
def review_vacate_request(
    request_id: int,
    req: VacateRequestReview,
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> VacateRequestResponse:
    """Approve or reject a vacate request. On approval, updates stay's approved vacating date."""
    return vacate_service.review_vacate_request(db, request_id, req, owner_user_id=current_user.id)
