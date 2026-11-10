from typing import List, Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, require_owner
from app.db.session import get_db
from app.models.models import User
from app.schemas.billing import BillingGenerateRequest, RentChargeResponse
from app.services import billing_service

router = APIRouter()


@router.post("/generate", response_model=List[RentChargeResponse], status_code=status.HTTP_201_CREATED)
def generate_bills(
    req: BillingGenerateRequest,
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> List[RentChargeResponse]:
    """Generate monthly rent charges using exact Decimal arithmetic."""
    return billing_service.generate_monthly_billing(
        db=db,
        year=req.year,
        month=req.month,
        actor_user_id=current_user.id,
    )


@router.get("/charges", response_model=List[RentChargeResponse], status_code=status.HTTP_200_OK)
def list_charges(
    tenant_id: Optional[int] = Query(None),
    year: Optional[int] = Query(None),
    month: Optional[int] = Query(None),
    status: Optional[str] = Query(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> List[RentChargeResponse]:
    """List rent charges. Tenants only see their own charges."""
    effective_tenant_id = current_user.id if current_user.role == "TENANT" else tenant_id
    return billing_service.list_rent_charges(
        db=db,
        tenant_id=effective_tenant_id,
        year=year,
        month=month,
        status=status,
    )


@router.get("/charges/{charge_id}", response_model=RentChargeResponse, status_code=status.HTTP_200_OK)
def get_charge(
    charge_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> RentChargeResponse:
    charge = billing_service.get_rent_charge_by_id(db, charge_id)
    # If user is a tenant, ensure charge belongs to them
    if current_user.role == "TENANT":
        user_charges = billing_service.list_rent_charges(db, tenant_id=current_user.id)
        if not any(c.id == charge.id for c in user_charges):
            from app.core.exceptions import ForbiddenError
            raise ForbiddenError("Access forbidden to this rent charge")
    return charge
