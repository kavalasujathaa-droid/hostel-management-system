from datetime import date
from decimal import Decimal
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.models.models import Room
from tests.conftest import auth_headers, create_test_owner, create_test_tenant


def test_room_crud_and_capacity_constraints(transactional_client: TestClient, db_session: Session):
    """Test room creation, unique room numbers, and positive capacity."""
    owner = create_test_owner(db_session, email="room_owner@test.com")
    headers = auth_headers(owner)

    # 1. Create Room
    res = transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "A-101", "floor": "1", "capacity": 2},
        headers=headers,
    )
    assert res.status_code == 201
    room_id = res.json()["id"]

    # 2. Duplicate Room Number blocked
    dup_res = transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "A-101", "floor": "1", "capacity": 2},
        headers=headers,
    )
    assert dup_res.status_code == 409

    # 3. Invalid capacity <= 0 blocked
    inv_res = transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "A-102", "floor": "1", "capacity": 0},
        headers=headers,
    )
    assert inv_res.status_code == 422


def test_room_occupancy_and_capacity_locking(transactional_client: TestClient, db_session: Session):
    """Test tenant assignment, available bed calculations, and overcapacity rejection."""
    owner = create_test_owner(db_session, email="occupancy_owner@test.com")
    headers = auth_headers(owner)

    # Create room with capacity = 1
    room_res = transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "Single-1", "floor": "Ground", "capacity": 1},
        headers=headers,
    )
    room_id = room_res.json()["id"]

    # Create two tenants
    t1 = create_test_tenant(db_session, email="tenant_one@test.com")
    t2 = create_test_tenant(db_session, email="tenant_two@test.com")

    # Assign t1 to room
    stay_res = transactional_client.post(
        "/api/v1/stays",
        json={
            "tenant_user_id": t1.id,
            "room_id": room_id,
            "joining_date": str(date.today()),
            "monthly_rent": 6500.00,
        },
        headers=headers,
    )
    assert stay_res.status_code == 201

    # Verify room occupancy: 1 active, 0 available
    detail_res = transactional_client.get(f"/api/v1/rooms/{room_id}", headers=headers)
    assert detail_res.status_code == 200
    assert detail_res.json()["active_occupants"] == 1
    assert detail_res.json()["available_beds"] == 0

    # Assign t2 to same room: blocked due to capacity lock
    overflow_res = transactional_client.post(
        "/api/v1/stays",
        json={
            "tenant_user_id": t2.id,
            "room_id": room_id,
            "joining_date": str(date.today()),
            "monthly_rent": 6500.00,
        },
        headers=headers,
    )
    assert overflow_res.status_code == 409
    assert "capacity" in overflow_res.json()["error"]["message"].lower()

    # Attempt to assign t1 again: blocked due to duplicate active stay
    dup_stay_res = transactional_client.post(
        "/api/v1/stays",
        json={
            "tenant_user_id": t1.id,
            "room_id": room_id,
            "joining_date": str(date.today()),
            "monthly_rent": 6500.00,
        },
        headers=headers,
    )
    assert dup_stay_res.status_code == 409

    # Deactivating room while occupied: blocked
    del_res = transactional_client.delete(f"/api/v1/rooms/{room_id}", headers=headers)
    assert del_res.status_code == 409
