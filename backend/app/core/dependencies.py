from typing import Optional
from fastapi import Depends, Header
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jwt.exceptions import ExpiredSignatureError, InvalidTokenError
from sqlalchemy.orm import Session

from app.core.exceptions import AuthenticationError, ForbiddenError
from app.core.security import decode_access_token
from app.db.session import get_db
from app.models.models import User

# Optional bearer schema so that we can customize 401 response explicitly
bearer_scheme = HTTPBearer(auto_error=False)


def get_current_user(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(bearer_scheme),
    db: Session = Depends(get_db),
) -> User:
    """Validate access token from Authorization header and return current User."""
    if not credentials or not credentials.credentials:
        raise AuthenticationError("Authentication token is missing")

    token = credentials.credentials
    try:
        payload = decode_access_token(token)
    except ExpiredSignatureError:
        raise AuthenticationError("Authentication token has expired")
    except InvalidTokenError:
        raise AuthenticationError("Invalid authentication token")
    except Exception:
        raise AuthenticationError("Could not validate credentials")

    user_id_str = payload.get("sub")
    if not user_id_str or not user_id_str.isdigit():
        raise AuthenticationError("Token payload missing subject identifier")

    user = db.query(User).filter(User.id == int(user_id_str)).first()
    if not user:
        raise AuthenticationError("User associated with token no longer exists")

    if not user.is_active:
        raise ForbiddenError("User account is inactive or disabled")

    return user


def require_active_user(current_user: User = Depends(get_current_user)) -> User:
    """Ensure current user is active."""
    if not current_user.is_active:
        raise ForbiddenError("User account is inactive")
    return current_user


def require_owner(current_user: User = Depends(require_active_user)) -> User:
    """Ensure current user has OWNER role."""
    if current_user.role != "OWNER":
        raise ForbiddenError("Access forbidden: Owner role required")
    return current_user


def require_tenant(current_user: User = Depends(require_active_user)) -> User:
    """Ensure current user has TENANT role."""
    if current_user.role != "TENANT":
        raise ForbiddenError("Access forbidden: Tenant role required")
    return current_user
