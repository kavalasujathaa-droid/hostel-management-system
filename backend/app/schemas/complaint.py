from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict, Field


class ComplaintCreate(BaseModel):
    title: str = Field(default="General Complaint", min_length=3, max_length=150)
    category: str = Field(default="Other")
    tenant_stay_id: Optional[int] = None
    description: str = Field(..., min_length=5)


class ComplaintUpdate(BaseModel):
    status: str = Field(..., pattern="^(OPEN|IN PROGRESS|RESOLVED)$")


class ComplaintResponse(BaseModel):
    id: int
    tenant_stay_id: int
    tenant_name: Optional[str] = None
    room_number: Optional[str] = None
    title: str = ""
    category: str = "Other"
    description: str
    status: str
    attachment_path: Optional[str] = None
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True)
