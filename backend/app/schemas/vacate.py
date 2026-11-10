from datetime import date, datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict, Field


class VacateRequestCreate(BaseModel):
    tenant_stay_id: Optional[int] = None  # If omitted, inferred from active stay
    requested_vacating_date: date


class VacateRequestReview(BaseModel):
    action: str = Field(..., pattern="^(APPROVE|REJECT)$")
    rejection_reason: Optional[str] = None


class VacateRequestResponse(BaseModel):
    id: int
    tenant_stay_id: int
    tenant_name: Optional[str] = None
    room_number: Optional[str] = None
    requested_vacating_date: date
    status: str
    reviewed_by: Optional[int] = None
    rejection_reason: Optional[str] = None
    requested_at: datetime
    reviewed_at: Optional[datetime] = None

    model_config = ConfigDict(from_attributes=True)
