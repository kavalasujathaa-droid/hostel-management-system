from typing import Any, Optional
from fastapi import Request, status
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException
from app.core.logging import logger


class AppException(Exception):
    """Base application exception."""

    def __init__(
        self,
        message: str = "An application error occurred",
        status_code: int = status.HTTP_500_INTERNAL_SERVER_ERROR,
        code: str = "APP_ERROR",
        details: Optional[Any] = None,
    ):
        self.message = message
        self.status_code = status_code
        self.code = code
        self.details = details
        super().__init__(self.message)


class AuthenticationError(AppException):
    """Raised when authentication fails or token is invalid/expired."""

    def __init__(
        self,
        message: str = "Could not validate credentials",
        code: str = "AUTHENTICATION_FAILED",
        details: Optional[Any] = None,
    ):
        super().__init__(
            message=message,
            status_code=status.HTTP_401_UNAUTHORIZED,
            code=code,
            details=details,
        )


class ForbiddenError(AppException):
    """Raised when user has insufficient permissions."""

    def __init__(
        self,
        message: str = "Insufficient permissions to perform this action",
        code: str = "FORBIDDEN",
        details: Optional[Any] = None,
    ):
        super().__init__(
            message=message,
            status_code=status.HTTP_403_FORBIDDEN,
            code=code,
            details=details,
        )


class NotFoundError(AppException):
    """Raised when a requested resource is not found."""

    def __init__(
        self,
        message: str = "Resource not found",
        code: str = "NOT_FOUND",
        details: Optional[Any] = None,
    ):
        super().__init__(
            message=message,
            status_code=status.HTTP_404_NOT_FOUND,
            code=code,
            details=details,
        )


class ConflictError(AppException):
    """Raised when a conflict occurs (duplicate record, unique constraint, full capacity)."""

    def __init__(
        self,
        message: str = "Resource conflict occurred",
        code: str = "CONFLICT",
        details: Optional[Any] = None,
    ):
        super().__init__(
            message=message,
            status_code=status.HTTP_409_CONFLICT,
            code=code,
            details=details,
        )


class ValidationError(AppException):
    """Raised when domain business rules fail validation."""

    def __init__(
        self,
        message: str = "Validation failed",
        code: str = "VALIDATION_FAILED",
        details: Optional[Any] = None,
    ):
        super().__init__(
            message=message,
            status_code=status.HTTP_400_BAD_REQUEST,
            code=code,
            details=details,
        )


class DatabaseConnectionError(AppException):
    """Raised when the database cannot be reached or fails readiness check."""

    def __init__(
        self,
        message: str = "Database connection unavailable",
        code: str = "DATABASE_UNAVAILABLE",
        details: Optional[Any] = None,
    ):
        super().__init__(
            message=message,
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            code=code,
            details=details,
        )


class ServiceUnavailableError(AppException):
    """Raised when an external or internal service is unavailable."""

    def __init__(
        self,
        message: str = "Service temporarily unavailable",
        code: str = "SERVICE_UNAVAILABLE",
        details: Optional[Any] = None,
    ):
        super().__init__(
            message=message,
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            code=code,
            details=details,
        )


def create_error_response(
    status_code: int,
    code: str,
    message: str,
    details: Optional[Any] = None,
) -> JSONResponse:
    """Generate a uniform JSON error response."""
    return JSONResponse(
        status_code=status_code,
        content={
            "success": False,
            "error": {
                "code": code,
                "message": message,
                "details": details,
            },
        },
    )


async def app_exception_handler(request: Request, exc: AppException) -> JSONResponse:
    """Handle custom application exceptions."""
    logger.error("AppException [%s]: %s (path: %s)", exc.code, exc.message, request.url.path)
    return create_error_response(
        status_code=exc.status_code,
        code=exc.code,
        message=exc.message,
        details=exc.details,
    )


async def http_exception_handler(request: Request, exc: StarletteHTTPException) -> JSONResponse:
    """Handle standard HTTP exceptions."""
    logger.warning("HTTPException [%d]: %s (path: %s)", exc.status_code, exc.detail, request.url.path)
    code = "HTTP_ERROR"
    if exc.status_code == 404:
        code = "NOT_FOUND"
    elif exc.status_code == 400:
        code = "BAD_REQUEST"
    elif exc.status_code == 401:
        code = "UNAUTHORIZED"
    elif exc.status_code == 403:
        code = "FORBIDDEN"
    elif exc.status_code == 409:
        code = "CONFLICT"
    elif exc.status_code == 503:
        code = "SERVICE_UNAVAILABLE"

    return create_error_response(
        status_code=exc.status_code,
        code=code,
        message=str(exc.detail),
    )


async def validation_exception_handler(request: Request, exc: RequestValidationError) -> JSONResponse:
    """Handle request validation errors."""
    logger.warning("ValidationError on %s: %s", request.url.path, exc.errors())
    return create_error_response(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        code="VALIDATION_ERROR",
        message="Invalid request payload or parameters",
        details=exc.errors(),
    )


async def global_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    """Catch-all unhandled exception handler to avoid leaking internal details."""
    logger.exception("Unhandled exception on %s: %s", request.url.path, str(exc))
    return create_error_response(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        code="INTERNAL_SERVER_ERROR",
        message="An unexpected internal error occurred. Please try again later.",
    )
