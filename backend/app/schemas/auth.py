from typing import Optional
from pydantic import BaseModel, ConfigDict, EmailStr, Field

from app.schemas.user import UserResponse


class OwnerInitRequest(BaseModel):
    full_name: str = Field(..., min_length=2, max_length=120)
    email: EmailStr
    password: str = Field(..., min_length=8)
    phone: Optional[str] = Field(None, max_length=20)
    hostel_name: str = Field(..., min_length=2, max_length=150)
    address: Optional[str] = None
    rent_due_day: int = Field(5, ge=1, le=28)
    bootstrap_secret: Optional[str] = None


class LoginRequest(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=1)


class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    expires_in: int
    user: UserResponse


class RefreshTokenRequest(BaseModel):
    refresh_token: str


class LogoutRequest(BaseModel):
    refresh_token: Optional[str] = None
