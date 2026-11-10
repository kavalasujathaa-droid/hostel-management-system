from datetime import date
from decimal import Decimal
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.services.billing_service import calculate_rent
from tests.conftest import auth_headers, create_test_owner, create_test_tenant


def test_exact_decimal_rent_calculations():
    """Verify exact Decimal arithmetic across full-month, partial-month, and leap years."""
    monthly_rent = Decimal("6000.00")

    # 1. Full 30-day month (e.g. November)
    days_m, billable_d, p_start, p_end, due = calculate_rent(
        monthly_rent=monthly_rent,
        year=2026,
        month=11,
        joining_date=date(2026, 1, 1),
    )
    assert days_m == 30
    assert billable_d == 30
    assert due == Decimal("6000.00")

    # 2. Mid-month joining on Nov 11 (20 billable days: Nov 11 to Nov 30 inclusive)
    # Daily rent = 6000 / 30 = 200.00; Amount due = 200.00 * 20 = 4000.00
    days_m, billable_d, p_start, p_end, due = calculate_rent(
        monthly_rent=monthly_rent,
        year=2026,
        month=11,
        joining_date=date(2026, 11, 11),
    )
    assert billable_d == 20
    assert due == Decimal("4000.00")

    # 3. Mid-month vacating on Nov 15 (15 billable days: Nov 1 to Nov 15 inclusive)
    # Daily rent = 200.00; Amount due = 200.00 * 15 = 3000.00
    days_m, billable_d, p_start, p_end, due = calculate_rent(
        monthly_rent=monthly_rent,
        year=2026,
        month=11,
        joining_date=date(2026, 1, 1),
        approved_vacating_date=date(2026, 11, 15),
    )
    assert billable_d == 15
    assert due == Decimal("3000.00")

    # 4. Leap Year February (2024: 29 days)
    # Monthly rent = 5500.00; Tenant joined Feb 15
    # Billable days = 29 - 15 + 1 = 15 days
    # (5500 / 29) * 15 = 2844.8275... rounds to 2844.83 with ROUND_HALF_UP
    days_m, billable_d, p_start, p_end, due = calculate_rent(
        monthly_rent=Decimal("5500.00"),
        year=2024,
        month=2,
        joining_date=date(2024, 2, 15),
    )
    assert days_m == 29
    assert billable_d == 15
    assert due == Decimal("2844.83")

    # 5. Non-leap Year February (2025: 28 days)
    # (5500 / 28) * 14 = 2750.00
    days_m, billable_d, p_start, p_end, due = calculate_rent(
        monthly_rent=Decimal("5500.00"),
        year=2025,
        month=2,
        joining_date=date(2025, 2, 15),
    )
    assert days_m == 28
    assert billable_d == 14
    assert due == Decimal("2750.00")


def test_billing_generation_and_duplicate_prevention(transactional_client: TestClient, db_session: Session):
    """Test generating monthly billing for active stays and duplicate rejection."""
    owner = create_test_owner(db_session, email="billing_owner@test.com")
    owner_headers = auth_headers(owner)

    # Create room & tenant
    room_res = transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "B-201", "floor": "2", "capacity": 2},
        headers=owner_headers,
    )
    room_id = room_res.json()["id"]

    tenant = create_test_tenant(db_session, email="bill_tenant@test.com")
    tenant_headers = auth_headers(tenant)

    # Assign stay starting Oct 1, 2026
    transactional_client.post(
        "/api/v1/stays",
        json={
            "tenant_user_id": tenant.id,
            "room_id": room_id,
            "joining_date": "2026-10-01",
            "monthly_rent": 7000.00,
        },
        headers=owner_headers,
    )

    # Generate billing for October 2026
    gen_res1 = transactional_client.post(
        "/api/v1/billing/generate",
        json={"year": 2026, "month": 10},
        headers=owner_headers,
    )
    assert gen_res1.status_code == 201
    charges = gen_res1.json()
    assert len(charges) >= 1

    # Second generation for same month must not duplicate charges (uq_rent_stay_billing_period)
    gen_res2 = transactional_client.post(
        "/api/v1/billing/generate",
        json={"year": 2026, "month": 10},
        headers=owner_headers,
    )
    assert gen_res2.status_code == 201
    charges2 = gen_res2.json()
    assert len(charges2) == len(charges)

    # Tenant queries their own charges
    tenant_charges = transactional_client.get("/api/v1/billing/charges", headers=tenant_headers)
    assert tenant_charges.status_code == 200
    assert len(tenant_charges.json()) == 1
    charge_data = tenant_charges.json()[0]
    assert Decimal(str(charge_data["amount_due"])) == Decimal("7000.00")
    assert Decimal(str(charge_data["balance"])) == Decimal("7000.00")
    assert charge_data["status"] in ("UNPAID", "OVERDUE")
