from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.core.config import settings
from app.core.exceptions import DatabaseConnectionError
from app.db.session import check_db_connectivity, get_db
from app.schemas.health import HealthLiveResponse, HealthReadyResponse

router = APIRouter()


@router.get(
    "/live",
    response_model=HealthLiveResponse,
    status_code=status.HTTP_200_OK,
    summary="Application liveness probe",
    description="Returns HTTP 200 if the application process is running and alive.",
)
def health_live() -> HealthLiveResponse:
    return HealthLiveResponse(
        status="ok",
        app=settings.APP_NAME,
        environment=settings.APP_ENV,
    )


@router.get(
    "/ready",
    response_model=HealthReadyResponse,
    status_code=status.HTTP_200_OK,
    summary="Application readiness probe",
    description="Verifies database connectivity with SELECT 1. Returns 200 when ready, 503 when database is unavailable.",
)
def health_ready(db: Session = Depends(get_db)) -> HealthReadyResponse:
    if not check_db_connectivity(db):
        raise DatabaseConnectionError(message="Database service is unavailable")
    return HealthReadyResponse(
        status="ready",
        database="connected",
    )
