from decimal import Decimal
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.services.notification_service import create_notification
from tests.conftest import auth_headers, create_test_owner, create_test_tenant


def test_notification_workflow(transactional_client: TestClient, db_session: Session):
    """Test notification retrieval and read status updates."""
    tenant = create_test_tenant(db_session, email="notif_tenant@test.com")
    headers = auth_headers(tenant)

    # Create in-app notifications
    n1 = create_notification(db_session, tenant.id, "Rent Due", "Rent is due on 5th", "RENT_BILL")
    n2 = create_notification(db_session, tenant.id, "Notice", "Maintenance scheduled", "GENERAL")
    db_session.flush()

    # List notifications
    list_res = transactional_client.get("/api/v1/notifications", headers=headers)
    assert list_res.status_code == 200
    notifs = list_res.json()
    assert len(notifs) >= 2

    # Mark single read
    read_res = transactional_client.put(f"/api/v1/notifications/{n1.id}/read", headers=headers)
    assert read_res.status_code == 200

    # Mark all read
    read_all_res = transactional_client.put("/api/v1/notifications/read-all", headers=headers)
    assert read_all_res.status_code == 200

    # Test category filtering
    create_notification(db_session, tenant.id, "Payment Accepted", "Payment of 5000 verified", "PAYMENT_APPROVED", category="PAYMENT")
    create_notification(db_session, tenant.id, "Rent Due Reminder", "Due in 2 days", "RENT_DUE_REMINDER", category="REMINDER")
    create_notification(db_session, tenant.id, "Overdue Notice", "Rent is overdue", "RENT_OVERDUE", category="ALERT")
    db_session.flush()

    # Filter by PAYMENT
    res_pay = transactional_client.get("/api/v1/notifications?category=PAYMENT", headers=headers)
    assert res_pay.status_code == 200
    assert all(item["category"] == "PAYMENT" for item in res_pay.json())

    # Filter by REMINDERS
    res_rem = transactional_client.get("/api/v1/notifications?category=REMINDERS", headers=headers)
    assert res_rem.status_code == 200
    assert all(item["category"] == "REMINDER" for item in res_rem.json())

    # Filter by ALERTS
    res_alert = transactional_client.get("/api/v1/notifications?category=ALERTS", headers=headers)
    assert res_alert.status_code == 200
    assert all(item["category"] == "ALERT" for item in res_alert.json())


def test_idempotent_rent_due_reminders(transactional_client: TestClient, db_session: Session):
    """Test idempotent automatic generation of rent due reminders and overdue alerts."""
    from datetime import date, timedelta
    from app.services.notification_service import generate_rent_due_reminders

    owner = create_test_owner(db_session, email="remind_owner@test.com")
    owner_headers = auth_headers(owner)

    tenant = create_test_tenant(db_session, email="remind_tenant@test.com")
    tenant_headers = auth_headers(tenant)

    # Setup room and stay
    room_res = transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "R-REM", "floor": "1", "capacity": 1},
        headers=owner_headers,
    )
    room_id = room_res.json()["id"]

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

    transactional_client.post(
        "/api/v1/billing/generate",
        json={"year": 2026, "month": 11},
        headers=owner_headers,
    )

    # First trigger: should process reminders
    trig1 = transactional_client.post("/api/v1/notifications/trigger-reminders", headers=owner_headers)
    assert trig1.status_code == 200
    assert trig1.json()["success"] is True

    # Immediate second trigger: should be idempotent (0 additional notifications)
    trig2 = transactional_client.post("/api/v1/notifications/trigger-reminders", headers=owner_headers)
    assert trig2.status_code == 200
    assert trig2.json()["result"]["reminders_sent"] == 0
    assert trig2.json()["result"]["overdue_sent"] == 0


def test_reports_and_dashboard_accuracy(transactional_client: TestClient, db_session: Session):
    """Test owner dashboard stats and financial report aggregations."""
    owner = create_test_owner(db_session, email="reports_owner@test.com")
    headers = auth_headers(owner)

    # Create room
    transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "R-101", "floor": "1", "capacity": 2},
        headers=headers,
    )

    # Check dashboard stats
    dash_res = transactional_client.get("/api/v1/reports/dashboard", headers=headers)
    assert dash_res.status_code == 200
    stats = dash_res.json()
    assert stats["total_rooms"] >= 1
    assert stats["total_capacity"] >= 2
    assert "occupancy_rate_percentage" in stats

    # Check financial report
    fin_res = transactional_client.get("/api/v1/reports/revenue", headers=headers)
    assert fin_res.status_code == 200
    fin_data = fin_res.json()
    assert "summary" in fin_data
    assert "monthly_breakdown" in fin_data


def test_device_token_registration_and_unregistration(transactional_client: TestClient, db_session: Session):
    """Test registering and unregistering FCM device tokens."""
    tenant = create_test_tenant(db_session, email="fcm_tenant@test.com")
    headers = auth_headers(tenant)

    # Register token
    reg_res = transactional_client.post(
        "/api/v1/notifications/device-token",
        json={"fcm_token": "fcm_test_token_12345", "platform": "ANDROID"},
        headers=headers,
    )
    assert reg_res.status_code == 200
    assert reg_res.json()["success"] is True

    # Re-register same token (idempotent / update)
    reg_res2 = transactional_client.post(
        "/api/v1/notifications/device-token",
        json={"fcm_token": "fcm_test_token_12345", "platform": "ANDROID"},
        headers=headers,
    )
    assert reg_res2.status_code == 200

    # Unregister token
    unreg_res = transactional_client.delete(
        "/api/v1/notifications/device-token?fcm_token=fcm_test_token_12345",
        headers=headers,
    )
    assert unreg_res.status_code == 200
    assert unreg_res.json()["success"] is True
