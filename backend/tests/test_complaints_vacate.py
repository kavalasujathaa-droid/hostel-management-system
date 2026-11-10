import io
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from tests.conftest import auth_headers, create_test_owner, create_test_tenant


def test_complaint_lifecycle(transactional_client: TestClient, db_session: Session):
    """Test complaint submission with title, category and owner status updates through RESOLVED."""
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

    # Tenant files complaint with title and category
    c_res = transactional_client.post(
        "/api/v1/complaints",
        json={
            "title": "Broken Switchboard",
            "category": "Electrical",
            "description": "Ceiling fan regulator is faulty and running on full speed only",
        },
        headers=tenant_headers,
    )
    assert c_res.status_code == 201
    c_data = c_res.json()
    assert c_data["status"] == "OPEN"
    assert c_data["title"] == "Broken Switchboard"
    assert c_data["category"] == "Electrical"
    assert c_data["attachment_path"] is None
    complaint_id = c_data["id"]

    # Owner updates to IN PROGRESS
    up_res = transactional_client.put(
        f"/api/v1/complaints/{complaint_id}",
        json={"status": "IN PROGRESS"},
        headers=owner_headers,
    )
    assert up_res.status_code == 200
    assert up_res.json()["status"] == "IN PROGRESS"

    # Owner updates to RESOLVED
    res_res = transactional_client.put(
        f"/api/v1/complaints/{complaint_id}",
        json={"status": "RESOLVED"},
        headers=owner_headers,
    )
    assert res_res.status_code == 200
    assert res_res.json()["status"] == "RESOLVED"

    # Invalid status outside ('OPEN', 'IN PROGRESS', 'RESOLVED') rejected by validation
    inv_res = transactional_client.put(
        f"/api/v1/complaints/{complaint_id}",
        json={"status": "CLOSED"},
        headers=owner_headers,
    )
    assert inv_res.status_code == 422


def test_complaint_with_attachment_and_auth(transactional_client: TestClient, db_session: Session):
    """Test complaint with photo attachment and role-based access to the attachment."""
    owner = create_test_owner(db_session, email="comp_attach_owner@test.com")
    owner_headers = auth_headers(owner)

    room_res = transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "C-102", "floor": "1", "capacity": 2},
        headers=owner_headers,
    )
    room_id = room_res.json()["id"]

    tenant1 = create_test_tenant(db_session, email="comp_t1@test.com")
    tenant1_headers = auth_headers(tenant1)

    tenant2 = create_test_tenant(db_session, email="comp_t2@test.com")
    tenant2_headers = auth_headers(tenant2)

    transactional_client.post(
        "/api/v1/stays",
        json={
            "tenant_user_id": tenant1.id,
            "room_id": room_id,
            "joining_date": "2026-11-01",
            "monthly_rent": 5000.00,
        },
        headers=owner_headers,
    )

    fake_image_bytes = b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01\x08\x06\x00\x00\x00\x1f\x15c4"
    files = {
        "attachment": ("leaking_tap.png", io.BytesIO(fake_image_bytes), "image/png"),
    }
    data = {
        "title": "Leaking Bathroom Pipe",
        "category": "Plumbing",
        "description": "The washbasin tap has a steady drip creating puddle.",
    }

    # 1. Tenant 1 submits complaint with photo
    c_res = transactional_client.post(
        "/api/v1/complaints",
        data=data,
        files=files,
        headers=tenant1_headers,
    )
    assert c_res.status_code == 201
    c_data = c_res.json()
    assert c_data["title"] == "Leaking Bathroom Pipe"
    assert c_data["category"] == "Plumbing"
    assert c_data["attachment_path"] is not None
    complaint_id = c_data["id"]

    # 2. Submitting tenant can download attachment
    dl_res = transactional_client.get(
        f"/api/v1/complaints/{complaint_id}/attachment",
        headers=tenant1_headers,
    )
    assert dl_res.status_code == 200
    assert dl_res.content == fake_image_bytes

    # 3. Owner can download attachment
    owner_dl_res = transactional_client.get(
        f"/api/v1/complaints/{complaint_id}/attachment",
        headers=owner_headers,
    )
    assert owner_dl_res.status_code == 200
    assert owner_dl_res.content == fake_image_bytes

    # 4. Another tenant cannot download attachment (403 Forbidden)
    t2_dl_res = transactional_client.get(
        f"/api/v1/complaints/{complaint_id}/attachment",
        headers=tenant2_headers,
    )
    assert t2_dl_res.status_code == 403

    # 5. Unauthenticated request cannot download attachment (401 Unauthorized)
    anon_dl_res = transactional_client.get(f"/api/v1/complaints/{complaint_id}/attachment")
    assert anon_dl_res.status_code == 401


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
