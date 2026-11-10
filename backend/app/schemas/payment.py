from datetime import datetime
from decimal import Decimal
from typing import Optional
from pydantic import BaseModel, ConfigDict, Field


class PaymentSubmissionRequest(BaseModel):
    rent_charge_id: int
    amount: Decimal = Field(..., gt=0)
    utr: str = Field(..., min_length=4, max_length=100)


class PaymentReviewRequest(BaseModel):
    rejection_reason: Optional[str] = None


class PaymentResponse(BaseModel):
    id: int
    rent_charge_id: int
    submitted_by: int
    submitted_by_name: Optional[str] = None
    reviewed_by: Optional[int] = None
    amount: Decimal
    utr: str
    status: str
    rejection_reason: Optional[str] = None
    proof_image_path: Optional[str] = None
    submitted_at: datetime
    reviewed_at: Optional[datetime] = None

    model_config = ConfigDict(from_attributes=True)
