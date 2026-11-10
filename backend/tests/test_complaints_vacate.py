from datetime import date
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from tests.conftest import auth_headers, create_test_owner, create_test_tenant


def test_complaint_lifecycle(transactional_client: TestClient, db_session: Session):
    """Test text-only complaint submission and owner status update."""
    owner = create_test_owner(db_session, email="comp_owner@test.com")
    owner_headers = auth_headers(owner)

    room_res = transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "C-101", "floor": "1", "capacity": 1},
        headers=owner_headers,
    )
    room_id = room_res.json()["id"]

    tenant = create_test_tenant(db_session, email="comp_tenant@test.com")
    tenant_headers = auth_headers(tenant)

    transactional_client.post(
        "/api/v1/stays",
        json={
            "tenant_user_id": tenant.id,
            "room_id": room_id,
            "joining_date": "2026-11-01",
            "monthly_rent": 4500.00,
        },
        headers=owner_headers,
    )

    # Tenant files complaint
    c_res = transactional_client.post(
        "/api/v1/complaints",
        json={"description": "Ceiling fan regulator is faulty and running on full speed only"},
        headers=tenant_headers,
    )
    assert c_res.status_code == 201
    assert c_res.json()["status"] == "OPEN"
    complaint_id = c_res.json()["id"]

    # Owner updates to IN PROGRESS
    up_res = transactional_client.put(
        f"/api/v1/complaints/{complaint_id}",
        json={"status": "IN PROGRESS"},
        headers=owner_headers,
    )
    assert up_res.status_code == 200
    assert up_res.json()["status"] == "IN PROGRESS"

    # Invalid status outside ('OPEN', 'IN PROGRESS') rejected by validation
    inv_res = transactional_client.put(
        f"/api/v1/complaints/{complaint_id}",
        json={"status": "CLOSED"},
        headers=owner_headers,
    )
    assert inv_res.status_code == 422


def test_vacate_request_workflow(transactional_client: TestClient, db_session: Session):
    """Test vacate request submission, validation against joining date, and owner approval."""
    owner = create_test_owner(db_session, email="vac_owner@test.com")
    owner_headers = auth_headers(owner)

    room_res = transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "V-101", "floor": "1", "capacity": 1},
        headers=owner_headers,
    )
    room_id = room_res.json()["id"]

    tenant = create_test_tenant(db_session, email="vac_tenant@test.com")
    tenant_headers = auth_headers(tenant)

    transactional_client.post(
        "/api/v1/stays",
        json={
            "tenant_user_id": tenant.id,
            "room_id": room_id,
            "joining_date": "2026-11-01",
            "monthly_rent": 5000.00,
        },
        headers=owner_headers,
    )

    # 1. Invalid vacate date earlier than joining date rejected
    inv_res = transactional_client.post(
        "/api/v1/vacate-requests",
        json={"requested_vacating_date": "2026-10-15"},
        headers=tenant_headers,
    )
    assert inv_res.status_code == 400

    # 2. Valid vacate request
    v_res = transactional_client.post(
        "/api/v1/vacate-requests",
        json={"requested_vacating_date": "2026-12-15"},
        headers=tenant_headers,
    )
    assert v_res.status_code == 201
    assert v_res.json()["status"] == "PENDING"
    req_id = v_res.json()["id"]

    # 3. Owner approves vacate request
    app_res = transactional_client.put(
        f"/api/v1/vacate-requests/{req_id}/review",
        json={"action": "APPROVE"},
        headers=owner_headers,
    )
    assert app_res.status_code == 200
    assert app_res.json()["status"] == "APPROVED"

    # Verify stay approved_vacating_date was updated
    active_stay = transactional_client.get("/api/v1/stays/active", headers=tenant_headers).json()
    assert active_stay["approved_vacating_date"] == "2026-12-15"
