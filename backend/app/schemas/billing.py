from datetime import date, datetime
from decimal import Decimal
from typing import Optional
from pydantic import BaseModel, ConfigDict, Field


class BillingGenerateRequest(BaseModel):
    year: int = Field(..., ge=2020, le=2100)
    month: int = Field(..., ge=1, le=12)


class RentChargeResponse(BaseModel):
    id: int
    tenant_stay_id: int
    tenant_name: Optional[str] = None
    room_number: Optional[str] = None
    billing_year: int
    billing_month: int
    period_start: date
    period_end: date
    monthly_rent_snapshot: Decimal
    days_in_month: int
    billable_days: int
    amount_due: Decimal
    amount_paid: Decimal
    balance: Decimal
    due_date: date
    status: str
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)
