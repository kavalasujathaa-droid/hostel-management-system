from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from tests.conftest import auth_headers, create_test_owner, create_test_tenant


def test_unauthenticated_request_blocked(transactional_client: TestClient):
    """Verify that unauthenticated requests to protected endpoints return 401 Unauthorized."""
    res = transactional_client.get("/api/v1/users/me")
    assert res.status_code == 401
    assert res.json()["error"]["code"] == "AUTHENTICATION_FAILED"


def test_tenant_blocked_from_owner_endpoints(transactional_client: TestClient, db_session: Session):
    """Verify that TENANT role users are blocked from OWNER-only endpoints with 403 Forbidden."""
    tenant = create_test_tenant(db_session, email="rbac_tenant@test.com")
    headers = auth_headers(tenant)

    # Attempt to create a room
    room_res = transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "101", "floor": "1", "capacity": 3},
        headers=headers,
    )
    assert room_res.status_code == 403
    assert room_res.json()["error"]["code"] == "FORBIDDEN"

    # Attempt to view owner dashboard
    dash_res = transactional_client.get("/api/v1/reports/dashboard", headers=headers)
    assert dash_res.status_code == 403

    # Attempt to generate monthly billing
    bill_res = transactional_client.post(
        "/api/v1/billing/generate",
        json={"year": 2026, "month": 11},
        headers=headers,
    )
    assert bill_res.status_code == 403


def test_owner_can_access_owner_endpoints(transactional_client: TestClient, db_session: Session):
    """Verify that OWNER role users can successfully access owner endpoints."""
    owner = create_test_owner(db_session, email="rbac_owner@test.com")
    headers = auth_headers(owner)

    # Room creation
    room_res = transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "201", "floor": "2", "capacity": 2},
        headers=headers,
    )
    assert room_res.status_code == 201

    # Dashboard view
    dash_res = transactional_client.get("/api/v1/reports/dashboard", headers=headers)
    assert dash_res.status_code == 200
