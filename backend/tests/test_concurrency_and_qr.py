import io
from concurrent.futures import ThreadPoolExecutor
from datetime import date
from decimal import Decimal
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.db.session import SessionLocal
from app.models.models import Payment, RentCharge, Room, TenantStay, User
from app.schemas.stay import TenantStayCreate
from app.services import payment_service, stay_service
from app.core.security import hash_password
from tests.conftest import auth_headers, create_test_owner


def test_concurrent_room_assignment_locking():
    """
    Simulate simultaneous concurrent requests attempting to assign two tenants
    to a single-capacity room. Verifies that atomic locking ensures exactly one succeeds.
    """
    db = SessionLocal()
    owner = User(full_name="Conc Room Owner", email="conc_ro@test.com", password_hash=hash_password("pwd"), role="OWNER", is_active=True)
    t1 = User(full_name="Conc Tenant 1", email="conc_rt1@test.com", password_hash=hash_password("pwd"), role="TENANT", is_active=True)
    t2 = User(full_name="Conc Tenant 2", email="conc_rt2@test.com", password_hash=hash_password("pwd"), role="TENANT", is_active=True)
    room = Room(room_number="CONC-R1", floor="1", capacity=1, is_active=True)

    db.add_all([owner, t1, t2, room])
    db.commit()

    owner_id = owner.id
    t1_id = t1.id
    t2_id = t2.id
    room_id = room.id
    today = date.today()
    db.close()

    results = []

    def attempt_assignment(tenant_id: int):
        local_db = SessionLocal()
        try:
            req = TenantStayCreate(
                tenant_user_id=tenant_id,
                room_id=room_id,
                joining_date=today,
                monthly_rent=Decimal("5000.00"),
            )
            stay_service.assign_tenant_to_room(local_db, req, actor_user_id=owner_id)
            results.append("SUCCESS")
        except Exception as exc:
            results.append(f"FAILED: {type(exc).__name__}: {exc}")
        finally:
            local_db.close()

    try:
        with ThreadPoolExecutor(max_workers=2) as executor:
            f1 = executor.submit(attempt_assignment, t1_id)
            f2 = executor.submit(attempt_assignment, t2_id)
            f1.result()
            f2.result()

        assert results.count("SUCCESS") == 1
        assert any("ConflictError" in r for r in results)
    finally:
        cleanup_db = SessionLocal()
        cleanup_db.query(TenantStay).filter(TenantStay.room_id == room_id).delete()
        cleanup_db.query(Room).filter(Room.id == room_id).delete()
        cleanup_db.query(User).filter(User.id.in_([owner_id, t1_id, t2_id])).delete()
        cleanup_db.commit()
        cleanup_db.close()


def test_concurrent_payment_approval_locking():
    """
    Simulate simultaneous concurrent owner review requests on the exact same payment.
    Verifies that row-level locking ensures exactly one approval succeeds and double-crediting is blocked.
    """
    db = SessionLocal()
    owner = User(full_name="Conc Pay Owner", email="conc_po@test.com", password_hash=hash_password("pwd"), role="OWNER", is_active=True)
    tenant = User(full_name="Conc Pay Tenant", email="conc_pt@test.com", password_hash=hash_password("pwd"), role="TENANT", is_active=True)
    room = Room(room_number="CONC-P1", floor="1", capacity=1, is_active=True)

    db.add_all([owner, tenant, room])
    db.flush()

    today = date.today()
    stay = TenantStay(
        tenant_user_id=tenant.id,
        room_id=room.id,
        joining_date=today,
        monthly_rent=Decimal("4000.00"),
        status="ACTIVE",
    )
    db.add(stay)
    db.flush()

    charge = RentCharge(
        tenant_stay_id=stay.id,
        billing_year=2026,
        billing_month=11,
        period_start=today,
        period_end=today,
        monthly_rent_snapshot=Decimal("4000.00"),
        days_in_month=30,
        billable_days=30,
        amount_due=Decimal("4000.00"),
        amount_paid=Decimal("0.00"),
        due_date=today,
        status="UNPAID",
    )
    db.add(charge)
    db.flush()

    payment = Payment(
        rent_charge_id=charge.id,
        submitted_by=tenant.id,
        amount=Decimal("4000.00"),
        utr="UTR_CONC_TEST_RUN",
        status="PENDING",
    )
    db.add(payment)
    db.commit()

    owner_id = owner.id
    payment_id = payment.id
    charge_id = charge.id
    stay_id = stay.id
    room_id = room.id
    tenant_id = tenant.id
    db.close()

    results = []

    def attempt_approval():
        local_db = SessionLocal()
        try:
            payment_service.review_payment(
                db=local_db,
                payment_id=payment_id,
                action="APPROVE",
                review_req=None,
                owner_user_id=owner_id,
            )
            results.append("SUCCESS")
        except Exception as exc:
            results.append(f"FAILED: {type(exc).__name__}: {exc}")
        finally:
            local_db.close()

    try:
        with ThreadPoolExecutor(max_workers=2) as executor:
            f1 = executor.submit(attempt_approval)
            f2 = executor.submit(attempt_approval)
            f1.result()
            f2.result()

        check_db = SessionLocal()
        refreshed_charge = check_db.query(RentCharge).filter(RentCharge.id == charge_id).one()
        final_paid = refreshed_charge.amount_paid
        check_db.close()

        assert results.count("SUCCESS") == 1
        assert final_paid == Decimal("4000.00")
    finally:
        cleanup_db = SessionLocal()
        cleanup_db.query(Payment).filter(Payment.id == payment_id).delete()
        cleanup_db.query(RentCharge).filter(RentCharge.id == charge_id).delete()
        cleanup_db.query(TenantStay).filter(TenantStay.id == stay_id).delete()
        cleanup_db.query(Room).filter(Room.id == room_id).delete()
        cleanup_db.query(User).filter(User.id.in_([owner_id, tenant_id])).delete()
        cleanup_db.commit()
        cleanup_db.close()


def test_qr_management_lifecycle(transactional_client: TestClient, db_session: Session):
    """Test UPI QR image upload, active QR retrieval, and single-active QR enforcement."""
    owner = create_test_owner(db_session, email="qr_owner_mgmt@test.com")
    owner_headers = auth_headers(owner)

    fake_png = b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01\x08\x06\x00\x00\x00\x1f\x15c4"

    # 1. Upload QR 1
    files1 = {"file": ("upi_qr1.png", io.BytesIO(fake_png), "image/png")}
    res1 = transactional_client.post("/api/v1/payment-qr", files=files1, headers=owner_headers)
    assert res1.status_code == 201
    qr1_id = res1.json()["id"]

    # 2. Verify active QR is QR 1
    active1 = transactional_client.get("/api/v1/payment-qr/active", headers=owner_headers)
    assert active1.status_code == 200
    assert active1.json()["id"] == qr1_id

    # 3. Upload QR 2 (must supersede QR 1)
    files2 = {"file": ("upi_qr2.png", io.BytesIO(fake_png), "image/png")}
    res2 = transactional_client.post("/api/v1/payment-qr", files=files2, headers=owner_headers)
    assert res2.status_code == 201
    qr2_id = res2.json()["id"]

    # 4. Verify active QR is now QR 2
    active2 = transactional_client.get("/api/v1/payment-qr/active", headers=owner_headers)
    assert active2.status_code == 200
    assert active2.json()["id"] == qr2_id
