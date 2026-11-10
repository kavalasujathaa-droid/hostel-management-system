from datetime import datetime
from pydantic import BaseModel, ConfigDict


class PaymentQRResponse(BaseModel):
    id: int
    image_path: str
    is_active: bool
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)
