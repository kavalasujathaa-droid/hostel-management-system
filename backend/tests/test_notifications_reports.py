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
