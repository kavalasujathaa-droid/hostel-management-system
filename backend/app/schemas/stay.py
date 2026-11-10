from datetime import date, datetime
from decimal import Decimal
from typing import Optional
from pydantic import BaseModel, ConfigDict, Field


class TenantStayCreate(BaseModel):
    tenant_user_id: int
    room_id: int
    joining_date: date
    monthly_rent: Decimal = Field(..., ge=0)


class TenantStayResponse(BaseModel):
    id: int
    tenant_user_id: int
    room_id: int
    room_number: Optional[str] = None
    joining_date: date
    approved_vacating_date: Optional[date] = None
    monthly_rent: Decimal
    status: str
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)
