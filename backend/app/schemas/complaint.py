from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict, Field


class ComplaintCreate(BaseModel):
    tenant_stay_id: Optional[int] = None  # Optional, if omitted will default to tenant's active stay
    description: str = Field(..., min_length=5)


class ComplaintUpdate(BaseModel):
    status: str = Field(..., pattern="^(OPEN|IN PROGRESS)$")


class ComplaintResponse(BaseModel):
    id: int
    tenant_stay_id: int
    tenant_name: Optional[str] = None
    room_number: Optional[str] = None
    description: str
    status: str
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True)
