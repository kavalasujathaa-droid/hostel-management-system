from datetime import datetime
from pydantic import BaseModel, ConfigDict


class NotificationResponse(BaseModel):
    id: int
    user_id: int
    title: str
    message: str
    notification_type: str
    category: str = "GENERAL"
    is_read: bool
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


class DeviceTokenRegisterRequest(BaseModel):
    fcm_token: str
    platform: str = "ANDROID"
