from datetime import datetime, timedelta, timezone
from typing import Optional
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.exceptions import (
    AuthenticationError,
    ConflictError,
    ForbiddenError,
    NotFoundError,
    ValidationError,
)
from app.core.security import (
    create_access_token,
    generate_refresh_token,
    hash_password,
    hash_refresh_token,
    verify_password,
)
from app.models.models import HostelSetting, RefreshToken, User
from app.schemas.auth import LoginRequest, OwnerInitRequest, TokenResponse
from app.schemas.user import UserResponse
from app.services.audit_service import log_audit_event


def init_owner(
    db: Session,
    req: OwnerInitRequest,
    ip_address: Optional[str] = None,
    device_info: Optional[str] = None,
) -> TokenResponse:
    """
    Secure one-time bootstrap mechanism to initialize the Hostel Owner & Settings.
    Guaranteed atomic execution with row-level safety.
    """
    # Verify bootstrap secret if configured in environment
    configured_secret = getattr(settings, "OWNER_BOOTSTRAP_SECRET", None)
    if configured_secret and req.bootstrap_secret != configured_secret:
        raise ForbiddenError("Invalid owner bootstrap secret")

    # Atomically lock and verify if an OWNER user or HostelSetting already exists
    existing_owner = db.query(User).filter(User.role == "OWNER").with_for_update().first()
    if existing_owner:
        raise ConflictError("Hostel Owner has already been initialized")

    existing_setting = db.query(HostelSetting).filter(HostelSetting.id == 1).first()
    if existing_setting:
        raise ConflictError("Hostel settings have already been initialized")

    # Check if email is already taken
    if db.query(User).filter(User.email == req.email).first():
        raise ConflictError(f"Email {req.email} is already registered")

    # Create Owner User
    owner = User(
        full_name=req.full_name,
        email=req.email,
        password_hash=hash_password(req.password),
        role="OWNER",
        phone=req.phone,
        is_active=True,
    )
    db.add(owner)
    db.flush()

    # Create Singleton HostelSetting (id = 1)
    hostel_setting = HostelSetting(
        id=1,
        owner_user_id=owner.id,
        hostel_name=req.hostel_name,
        address=req.address,
        rent_due_day=req.rent_due_day,
        currency="INR",
    )
    db.add(hostel_setting)
    db.flush()

    # Issue Tokens
    access_token = create_access_token(subject=owner.id, role=owner.role, email=owner.email)
    plain_refresh, refresh_hash = generate_refresh_token()
    refresh_expires = datetime.now(timezone.utc) + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)

    refresh_record = RefreshToken(
        user_id=owner.id,
        token_hash=refresh_hash,
        device_info=device_info,
        ip_address=ip_address,
        expires_at=refresh_expires.replace(tzinfo=None),
        is_revoked=False,
    )
    db.add(refresh_record)

    log_audit_event(
        db=db,
        action="OWNER_BOOTSTRAP_INITIALIZED",
        entity_type="USER",
        entity_id=owner.id,
        actor_user_id=owner.id,
        details={"hostel_name": req.hostel_name, "email": req.email},
    )

    db.commit()
    db.refresh(owner)

    return TokenResponse(
        access_token=access_token,
        refresh_token=plain_refresh,
        expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
        user=UserResponse.model_validate(owner),
    )


def login(
    db: Session,
    req: LoginRequest,
    ip_address: Optional[str] = None,
    device_info: Optional[str] = None,
) -> TokenResponse:
    """Authenticate user with email and password, returning JWT access and database refresh token."""
    user = db.query(User).filter(User.email == req.email).first()
    if not user or not verify_password(req.password, user.password_hash):
        raise AuthenticationError("Invalid email or password")

    if not user.is_active:
        raise ForbiddenError("Account is inactive or disabled")

    access_token = create_access_token(subject=user.id, role=user.role, email=user.email)
    plain_refresh, refresh_hash = generate_refresh_token()
    refresh_expires = datetime.now(timezone.utc) + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)

    refresh_record = RefreshToken(
        user_id=user.id,
        token_hash=refresh_hash,
        device_info=device_info,
        ip_address=ip_address,
        expires_at=refresh_expires.replace(tzinfo=None),
        is_revoked=False,
    )
    db.add(refresh_record)

    log_audit_event(
        db=db,
        action="USER_LOGIN",
        entity_type="USER",
        entity_id=user.id,
        actor_user_id=user.id,
        details={"ip": ip_address},
    )

    db.commit()
    db.refresh(user)

    return TokenResponse(
        access_token=access_token,
        refresh_token=plain_refresh,
        expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
        user=UserResponse.model_validate(user),
    )


def refresh_tokens(
    db: Session,
    plain_refresh_token: str,
    ip_address: Optional[str] = None,
    device_info: Optional[str] = None,
) -> TokenResponse:
    """
    Rotate refresh token securely:
    - Validates token against database-backed hash.
    - If already revoked: detects replay attack and invalidates all user tokens.
    - If valid: revokes current token and issues a new access/refresh pair.
    """
    token_hash = hash_refresh_token(plain_refresh_token)
    token_record = (
        db.query(RefreshToken)
        .filter(RefreshToken.token_hash == token_hash)
        .with_for_update()
        .first()
    )

    if not token_record:
        raise AuthenticationError("Invalid refresh token")

    # Replay Attack Detection (RFC 6819)
    if token_record.is_revoked:
        # Revoke all active tokens for this user
        db.query(RefreshToken).filter(
            RefreshToken.user_id == token_record.user_id,
            RefreshToken.is_revoked == False,  # noqa: E712
        ).update({"is_revoked": True, "revoked_at": datetime.utcnow()})
        db.commit()

        log_audit_event(
            db=db,
            action="SECURITY_ALERT_REFRESH_TOKEN_REPLAY",
            entity_type="USER",
            entity_id=token_record.user_id,
            actor_user_id=token_record.user_id,
            details={"ip": ip_address, "replayed_hash": token_hash[:16]},
        )
        raise AuthenticationError("Revoked refresh token replayed. All sessions have been terminated.")

    # Expiry check
    if token_record.expires_at < datetime.utcnow():
        token_record.is_revoked = True
        token_record.revoked_at = datetime.utcnow()
        db.commit()
        raise AuthenticationError("Refresh token has expired")

    user = db.query(User).filter(User.id == token_record.user_id).first()
    if not user or not user.is_active:
        raise ForbiddenError("User account is inactive or disabled")

    # Rotate: Revoke current token
    token_record.is_revoked = True
    token_record.revoked_at = datetime.utcnow()

    # Generate new token pair
    access_token = create_access_token(subject=user.id, role=user.role, email=user.email)
    new_plain_refresh, new_refresh_hash = generate_refresh_token()
    refresh_expires = datetime.now(timezone.utc) + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)

    new_refresh_record = RefreshToken(
        user_id=user.id,
        token_hash=new_refresh_hash,
        device_info=device_info,
        ip_address=ip_address,
        expires_at=refresh_expires.replace(tzinfo=None),
        is_revoked=False,
    )
    db.add(new_refresh_record)
    db.commit()

    return TokenResponse(
        access_token=access_token,
        refresh_token=new_plain_refresh,
        expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
        user=UserResponse.model_validate(user),
    )


def logout(
    db: Session,
    user_id: int,
    plain_refresh_token: Optional[str] = None,
) -> None:
    """Revoke refresh token(s) and record audit log."""
    if plain_refresh_token:
        token_hash = hash_refresh_token(plain_refresh_token)
        db.query(RefreshToken).filter(
            RefreshToken.user_id == user_id,
            RefreshToken.token_hash == token_hash,
        ).update({"is_revoked": True, "revoked_at": datetime.utcnow()})
    else:
        # Revoke all active tokens for this user
        db.query(RefreshToken).filter(
            RefreshToken.user_id == user_id,
            RefreshToken.is_revoked == False,  # noqa: E712
        ).update({"is_revoked": True, "revoked_at": datetime.utcnow()})

    log_audit_event(
        db=db,
        action="USER_LOGOUT",
        entity_type="USER",
        entity_id=user_id,
        actor_user_id=user_id,
    )
    db.commit()


def change_password(
    db: Session,
    user: User,
    old_password: str,
    new_password: str,
) -> None:
    """Verify old password with Argon2id, update to new Argon2id hash, and invalidate all refresh sessions."""
    if not verify_password(old_password, user.password_hash):
        raise ValidationError("Incorrect current password")

    user.password_hash = hash_password(new_password)
    user.updated_at = datetime.utcnow()

    # Invalidate all active refresh tokens upon password change
    db.query(RefreshToken).filter(
        RefreshToken.user_id == user.id,
        RefreshToken.is_revoked == False,  # noqa: E712
    ).update({"is_revoked": True, "revoked_at": datetime.utcnow()})

    log_audit_event(
        db=db,
        action="USER_PASSWORD_CHANGED",
        entity_type="USER",
        entity_id=user.id,
        actor_user_id=user.id,
    )
    db.commit()
