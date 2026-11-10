from decimal import Decimal
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from tests.conftest import auth_headers, create_test_owner, create_test_tenant


def test_payment_submission_and_utr_uniqueness(transactional_client: TestClient, db_session: Session):
    """Test payment submission, unique UTR enforcement, and overpayment prevention."""
    owner = create_test_owner(db_session, email="pay_owner@test.com")
    owner_headers = auth_headers(owner)

    # Room and tenant setup
    room_res = transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "P-101", "floor": "1", "capacity": 1},
        headers=owner_headers,
    )
    room_id = room_res.json()["id"]

    tenant = create_test_tenant(db_session, email="pay_tenant@test.com")
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

    # Generate billing for Nov 2026
    transactional_client.post(
        "/api/v1/billing/generate",
        json={"year": 2026, "month": 11},
        headers=owner_headers,
    )
    charges = transactional_client.get("/api/v1/billing/charges", headers=tenant_headers).json()
    charge_id = charges[0]["id"]

    # 1. Successful partial payment submission
    p_res = transactional_client.post(
        "/api/v1/payments",
        json={"rent_charge_id": charge_id, "amount": 2000.00, "utr": "UTR1234567890"},
        headers=tenant_headers,
    )
    assert p_res.status_code == 201
    assert p_res.json()["status"] == "PENDING"
    payment_id = p_res.json()["id"]

    # 2. Duplicate UTR submission: blocked with 409 Conflict
    dup_res = transactional_client.post(
        "/api/v1/payments",
        json={"rent_charge_id": charge_id, "amount": 1000.00, "utr": "UTR1234567890"},
        headers=tenant_headers,
    )
    assert dup_res.status_code == 409
    assert "utr" in dup_res.json()["error"]["message"].lower()

    # 3. Overpayment submission: blocked (amount > remaining balance 5000.00)
    over_res = transactional_client.post(
        "/api/v1/payments",
        json={"rent_charge_id": charge_id, "amount": 9999.00, "utr": "UTR9999999999"},
        headers=tenant_headers,
    )
    assert over_res.status_code == 400


def test_payment_approval_and_rejection_lifecycle(transactional_client: TestClient, db_session: Session):
    """Test owner approval (updating balances atomically) and rejection workflows."""
    owner = create_test_owner(db_session, email="approve_owner@test.com")
    owner_headers = auth_headers(owner)

    # Setup room and tenant
    room_res = transactional_client.post(
        "/api/v1/rooms",
        json={"room_number": "P-202", "floor": "2", "capacity": 1},
        headers=owner_headers,
    )
    room_id = room_res.json()["id"]

    tenant = create_test_tenant(db_session, email="approve_tenant@test.com")
    tenant_headers = auth_headers(tenant)

    transactional_client.post(
        "/api/v1/stays",
        json={
            "tenant_user_id": tenant.id,
            "room_id": room_id,
            "joining_date": "2026-11-01",
            "monthly_rent": 4000.00,
        },
        headers=owner_headers,
    )

    transactional_client.post(
        "/api/v1/billing/generate",
        json={"year": 2026, "month": 11},
        headers=owner_headers,
    )
    charge_id = transactional_client.get("/api/v1/billing/charges", headers=tenant_headers).json()[0]["id"]

    # Partial payment 1: 1500.00
    p1 = transactional_client.post(
        "/api/v1/payments",
        json={"rent_charge_id": charge_id, "amount": 1500.00, "utr": "UTR_PARTIAL_1"},
        headers=tenant_headers,
    ).json()

    # Owner approves payment 1
    app_res = transactional_client.put(f"/api/v1/payments/{p1['id']}/approve", headers=owner_headers)
    assert app_res.status_code == 200
    assert app_res.json()["status"] == "APPROVED"

    # Verify charge updated to PARTIALLY_PAID with balance 2500.00
    charge_state = transactional_client.get(f"/api/v1/billing/charges/{charge_id}", headers=owner_headers).json()
    assert charge_state["status"] == "PARTIALLY_PAID"
    assert Decimal(str(charge_state["amount_paid"])) == Decimal("1500.00")
    assert Decimal(str(charge_state["balance"])) == Decimal("2500.00")

    # Double approval of same payment blocked with 409
    dup_app = transactional_client.put(f"/api/v1/payments/{p1['id']}/approve", headers=owner_headers)
    assert dup_app.status_code == 409

    # Partial payment 2 (to be rejected): 1000.00
    p2 = transactional_client.post(
        "/api/v1/payments",
        json={"rent_charge_id": charge_id, "amount": 1000.00, "utr": "UTR_TO_REJECT"},
        headers=tenant_headers,
    ).json()

    # Owner rejects payment 2
    rej_res = transactional_client.put(
        f"/api/v1/payments/{p2['id']}/reject",
        json={"rejection_reason": "UTR mismatch on bank statement"},
        headers=owner_headers,
    )
    assert rej_res.status_code == 200
    assert rej_res.json()["status"] == "REJECTED"
    assert rej_res.json()["rejection_reason"] == "UTR mismatch on bank statement"

    # Verify charge balance unaffected by rejected payment
    charge_state2 = transactional_client.get(f"/api/v1/billing/charges/{charge_id}", headers=owner_headers).json()
    assert Decimal(str(charge_state2["amount_paid"])) == Decimal("1500.00")
    assert Decimal(str(charge_state2["balance"])) == Decimal("2500.00")

    # Full remaining payment: 2500.00
    p3 = transactional_client.post(
        "/api/v1/payments",
        json={"rent_charge_id": charge_id, "amount": 2500.00, "utr": "UTR_FULL_PAID"},
        headers=tenant_headers,
    ).json()
    transactional_client.put(f"/api/v1/payments/{p3['id']}/approve", headers=owner_headers)

    # Verify charge status is now PAID
    final_charge = transactional_client.get(f"/api/v1/billing/charges/{charge_id}", headers=owner_headers).json()
    assert final_charge["status"] == "PAID"
    assert Decimal(str(final_charge["balance"])) == Decimal("0.00")
