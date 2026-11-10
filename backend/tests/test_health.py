from unittest.mock import patch
from fastapi.testclient import TestClient

from app.core.config import settings
from app.db.session import get_db
from app.main import app


def test_application_startup(client: TestClient):
    """Verify application starts and serves root info endpoint."""
    response = client.get("/")
    assert response.status_code == 200
    data = response.json()
    assert data["app"] == settings.APP_NAME
    assert data["status"] == "online"
    assert data["api_v1"] == settings.API_V1_PREFIX


def test_health_live_endpoint(client: TestClient):
    """Verify /api/v1/health/live returns HTTP 200 and liveness metadata."""
    response = client.get(f"{settings.API_V1_PREFIX}/health/live")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert data["app"] == settings.APP_NAME
    assert data["environment"] == settings.APP_ENV


def test_health_ready_endpoint_success(client: TestClient):
    """Verify /api/v1/health/ready returns HTTP 200 when database connectivity is functional."""
    response = client.get(f"{settings.API_V1_PREFIX}/health/ready")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ready"
    assert data["database"] == "connected"


def test_health_ready_endpoint_db_failure(client: TestClient):
    """
    Verify /api/v1/health/ready returns HTTP 503 when the database check fails,
    and returns a clean JSON error response without exposing internal secrets.
    """
    with patch("app.api.v1.endpoints.health.check_db_connectivity", return_value=False):
        response = client.get(f"{settings.API_V1_PREFIX}/health/ready")
        assert response.status_code == 503
        data = response.json()
        assert data["success"] is False
        assert data["error"]["code"] == "DATABASE_UNAVAILABLE"
        assert "Database service is unavailable" in data["error"]["message"]

        # Ensure no credential leakage
        raw_text = response.text
        assert settings.TIDB_PASSWORD not in raw_text or settings.TIDB_PASSWORD == ""
        assert "mysql+pymysql" not in raw_text


def test_security_headers_and_static_mount(client: TestClient):
    """Verify production security headers are set and static /uploads path is accessible."""
    response = client.get("/")
    assert response.status_code == 200
    assert response.headers["X-Content-Type-Options"] == "nosniff"
    assert response.headers["X-Frame-Options"] == "DENY"
    assert response.headers["X-XSS-Protection"] == "1; mode=block"
    assert response.headers["Referrer-Policy"] == "strict-origin-when-cross-origin"

    # Verify static directory mounting (404 for missing file, not 500)
    static_resp = client.get("/uploads/qr/non_existent_file.png")
    assert static_resp.status_code == 404
