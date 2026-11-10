from fastapi import APIRouter, Depends, Request, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user
from app.db.session import get_db
from app.models.models import User
from app.schemas.auth import LoginRequest, LogoutRequest, OwnerInitRequest, RefreshTokenRequest, TokenResponse
from app.schemas.user import UserResponse
from app.services import auth_service

router = APIRouter()


@router.post(
    "/init-owner",
    response_model=TokenResponse,
    status_code=status.HTTP_201_CREATED,
    summary="One-time owner initialization bootstrap",
)
def initialize_owner(
    req: OwnerInitRequest,
    request: Request,
    db: Session = Depends(get_db),
) -> TokenResponse:
    ip = request.client.host if request.client else None
    user_agent = request.headers.get("User-Agent")
    return auth_service.init_owner(db, req, ip_address=ip, device_info=user_agent)


@router.post(
    "/login",
    response_model=TokenResponse,
    status_code=status.HTTP_200_OK,
    summary="Authenticate user and issue JWT access and database refresh tokens",
)
def login(
    req: LoginRequest,
    request: Request,
    db: Session = Depends(get_db),
) -> TokenResponse:
    ip = request.client.host if request.client else None
    user_agent = request.headers.get("User-Agent")
    return auth_service.login(db, req, ip_address=ip, device_info=user_agent)


@router.post(
    "/refresh",
    response_model=TokenResponse,
    status_code=status.HTTP_200_OK,
    summary="Rotate refresh token and issue new token pair",
)
def refresh_token(
    req: RefreshTokenRequest,
    request: Request,
    db: Session = Depends(get_db),
) -> TokenResponse:
    ip = request.client.host if request.client else None
    user_agent = request.headers.get("User-Agent")
    return auth_service.refresh_tokens(db, req.refresh_token, ip_address=ip, device_info=user_agent)


@router.post(
    "/logout",
    status_code=status.HTTP_200_OK,
    summary="Revoke refresh token and terminate session",
)
def logout(
    req: LogoutRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    auth_service.logout(db, user_id=current_user.id, plain_refresh_token=req.refresh_token)
    return {"success": True, "message": "Successfully logged out"}


@router.get(
    "/me",
    response_model=UserResponse,
    status_code=status.HTTP_200_OK,
    summary="Get current user details",
)
def get_me(current_user: User = Depends(get_current_user)) -> UserResponse:
    return UserResponse.model_validate(current_user)
