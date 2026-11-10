from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, ConfigDict, Field


class RoomBase(BaseModel):
    room_number: str = Field(..., min_length=1, max_length=30)
    floor: Optional[str] = Field(None, max_length=30)
    capacity: int = Field(..., gt=0)


class RoomCreate(RoomBase):
    pass


class RoomUpdate(BaseModel):
    floor: Optional[str] = Field(None, max_length=30)
    capacity: Optional[int] = Field(None, gt=0)
    is_active: Optional[bool] = None


class OccupantInfo(BaseModel):
    tenant_id: int
    full_name: str
    email: str
    joining_date: str
    stay_id: int

    model_config = ConfigDict(from_attributes=True)


class RoomResponse(RoomBase):
    id: int
    is_active: bool
    created_at: datetime
    active_occupants: int = 0
    available_beds: int = 0
    occupants: Optional[List[OccupantInfo]] = None

    model_config = ConfigDict(from_attributes=True)
