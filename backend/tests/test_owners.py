from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.core.security import verify_password
from app.models.models import AuditLog, HostelSetting, User
from tests.conftest import auth_headers, create_test_owner, create_test_tenant


def test_existing_owner_creates_second_owner_successfully(transactional_client: TestClient, db_session: Session):
    """1. Existing Owner creates a second Owner successfully."""
    owner1 = create_test_owner(db_session, email="owner1@hostel.com")
    headers = auth_headers(owner1)

    payload = {
        "full_name": "Co-Owner Alice",
        "email": "alice.owner@hostel.com",
        "password": "SecurePassword123",
        "phone": "9876543210",
    }
    res = transactional_client.post("/api/v1/owners", json=payload, headers=headers)
    assert res.status_code == 201
    data = res.json()
    assert data["full_name"] == "Co-Owner Alice"
    assert data["email"] == "alice.owner@hostel.com"
    assert data["role"] == "OWNER"
    assert data["is_active"] is True
    assert "id" in data


def test_second_owner_logs_in_successfully(transactional_client: TestClient, db_session: Session):
    """2. Second Owner logs in successfully."""
    owner1 = create_test_owner(db_session, email="owner_lead@hostel.com")
    headers1 = auth_headers(owner1)

    # Owner 1 creates Owner 2
    res_create = transactional_client.post(
        "/api/v1/owners",
        json={
            "full_name": "Co-Owner Bob",
            "email": "bob.owner@hostel.com",
            "password": "BobPassword888",
            "phone": "9998887776",
        },
        headers=headers1,
    )
    assert res_create.status_code == 201

    # Owner 2 logs in
    login_res = transactional_client.post(
        "/api/v1/auth/login",
        json={"email": "bob.owner@hostel.com", "password": "BobPassword888"},
    )
    assert login_res.status_code == 200
    login_data = login_res.json()
    assert "access_token" in login_data
    assert login_data["user"]["role"] == "OWNER"
    assert login_data["user"]["email"] == "bob.owner@hostel.com"


def test_second_owner_accesses_owner_dashboard(transactional_client: TestClient, db_session: Session):
    """3. Second Owner accesses Owner dashboard."""
    owner1 = create_test_owner(db_session, email="owner_head@hostel.com")
    headers1 = auth_headers(owner1)

    transactional_client.post(
        "/api/v1/owners",
        json={
            "full_name": "Co-Owner Charlie",
            "email": "charlie@hostel.com",
            "password": "CharliePassword999",
        },
        headers=headers1,
    )

    owner2 = db_session.query(User).filter(User.email == "charlie@hostel.com").first()
    assert owner2 is not None
    headers2 = auth_headers(owner2)

    # Access owner dashboard
    dash_res = transactional_client.get("/api/v1/reports/dashboard", headers=headers2)
    assert dash_res.status_code == 200
    assert "total_rooms" in dash_res.json()


def test_second_owner_creates_third_owner(transactional_client: TestClient, db_session: Session):
    """4. Second Owner can also create a third Owner."""
    owner1 = create_test_owner(db_session, email="owner_first@hostel.com")
    headers1 = auth_headers(owner1)

    transactional_client.post(
        "/api/v1/owners",
        json={
            "full_name": "Co-Owner Two",
            "email": "two@hostel.com",
            "password": "PasswordTwo222",
        },
        headers=headers1,
    )
    owner2 = db_session.query(User).filter(User.email == "two@hostel.com").first()
    headers2 = auth_headers(owner2)

    # Owner 2 creates Owner 3
    res3 = transactional_client.post(
        "/api/v1/owners",
        json={
            "full_name": "Co-Owner Three",
            "email": "three@hostel.com",
            "password": "PasswordThree333",
        },
        headers=headers2,
    )
    assert res3.status_code == 201
    assert res3.json()["email"] == "three@hostel.com"
    assert res3.json()["role"] == "OWNER"


def test_duplicate_email_returns_409(transactional_client: TestClient, db_session: Session):
    """5. Duplicate email returns 409 Conflict."""
    owner1 = create_test_owner(db_session, email="owner_orig@hostel.com")
    headers = auth_headers(owner1)

    # Attempt to create owner with existing email
    res = transactional_client.post(
        "/api/v1/owners",
        json={
            "full_name": "Duplicate Owner",
            "email": "owner_orig@hostel.com",
            "password": "AnyPassword123",
        },
        headers=headers,
    )
    assert res.status_code == 409
    assert res.json()["error"]["code"] == "CONFLICT"


def test_tenant_cannot_create_owner(transactional_client: TestClient, db_session: Session):
    """6. Tenant cannot create Owner (403 Forbidden)."""
    tenant = create_test_tenant(db_session, email="tenant_intruder@hostel.com")
    headers = auth_headers(tenant)

    res = transactional_client.post(
        "/api/v1/owners",
        json={
            "full_name": "Illegal Owner",
            "email": "illegal@hostel.com",
            "password": "IllegalPassword123",
        },
        headers=headers,
    )
    assert res.status_code == 403
    assert res.json()["error"]["code"] == "FORBIDDEN"


def test_tenant_cannot_list_owners(transactional_client: TestClient, db_session: Session):
    """7. Tenant cannot list Owners (403 Forbidden)."""
    tenant = create_test_tenant(db_session, email="tenant_reader@hostel.com")
    headers = auth_headers(tenant)

    res = transactional_client.get("/api/v1/owners", headers=headers)
    assert res.status_code == 403
    assert res.json()["error"]["code"] == "FORBIDDEN"


def test_unauthenticated_user_receives_401(transactional_client: TestClient):
    """8. Unauthenticated user receives 401 Unauthorized."""
    res_get = transactional_client.get("/api/v1/owners")
    assert res_get.status_code == 401

    res_post = transactional_client.post(
        "/api/v1/owners",
        json={"full_name": "No Auth", "email": "noauth@test.com", "password": "Password123"},
    )
    assert res_post.status_code == 401


def test_password_is_saved_as_argon2_hash(transactional_client: TestClient, db_session: Session):
    """9. Password is saved as Argon2 hash."""
    owner = create_test_owner(db_session, email="owner_hasher@hostel.com")
    headers = auth_headers(owner)

    plain_pass = "MySecretPass999!"
    res = transactional_client.post(
        "/api/v1/owners",
        json={
            "full_name": "Hashed Owner",
            "email": "hashed@hostel.com",
            "password": plain_pass,
        },
        headers=headers,
    )
    assert res.status_code == 201

    new_owner = db_session.query(User).filter(User.email == "hashed@hostel.com").first()
    assert new_owner is not None
    assert new_owner.password_hash.startswith("$argon2")
    assert verify_password(plain_pass, new_owner.password_hash)


def test_password_hash_is_absent_from_api_response(transactional_client: TestClient, db_session: Session):
    """10. Password hash is absent from API response."""
    owner = create_test_owner(db_session, email="owner_safe@hostel.com")
    headers = auth_headers(owner)

    res_post = transactional_client.post(
        "/api/v1/owners",
        json={
            "full_name": "Safe Owner",
            "email": "safe@hostel.com",
            "password": "SafePassword123",
        },
        headers=headers,
    )
    assert res_post.status_code == 201
    assert "password" not in res_post.json()
    assert "password_hash" not in res_post.json()

    res_get = transactional_client.get("/api/v1/owners", headers=headers)
    assert res_get.status_code == 200
    for o in res_get.json():
        assert "password" not in o
        assert "password_hash" not in o


def test_owner_listing_returns_only_owner_accounts(transactional_client: TestClient, db_session: Session):
    """11. Owner listing returns only OWNER accounts."""
    owner = create_test_owner(db_session, email="owner_filter@hostel.com")
    create_test_tenant(db_session, email="tenant_filter1@hostel.com")
    create_test_tenant(db_session, email="tenant_filter2@hostel.com")
    headers = auth_headers(owner)

    res = transactional_client.get("/api/v1/owners", headers=headers)
    assert res.status_code == 200
    owners = res.json()
    assert len(owners) >= 1
    for o in owners:
        assert o["role"] == "OWNER"


def test_audit_log_is_created_on_owner_creation(transactional_client: TestClient, db_session: Session):
    """12. Audit log is created on owner creation."""
    owner = create_test_owner(db_session, email="owner_auditor@hostel.com")
    headers = auth_headers(owner)

    res = transactional_client.post(
        "/api/v1/owners",
        json={
            "full_name": "Audit Target",
            "email": "audit.target@hostel.com",
            "password": "AuditPassword123",
        },
        headers=headers,
    )
    assert res.status_code == 201
    created_id = res.json()["id"]

    audit_entry = (
        db_session.query(AuditLog)
        .filter(AuditLog.action == "OWNER_ACCOUNT_CREATED", AuditLog.entity_id == created_id)
        .first()
    )
    assert audit_entry is not None
    assert audit_entry.actor_user_id == owner.id
    assert audit_entry.details["email"] == "audit.target@hostel.com"


def test_initial_init_owner_remains_one_time_only(transactional_client: TestClient, db_session: Session):
    """13. Initial init-owner remains one-time only."""
    create_test_owner(db_session, email="bootstrap_done@hostel.com")

    # Attempt to call init-owner when owner already exists
    res = transactional_client.post(
        "/api/v1/auth/init-owner",
        json={
            "full_name": "Second Init",
            "email": "second_init@hostel.com",
            "password": "Password123",
            "hostel_name": "Second Hostel",
            "address": "Street 2",
            "rent_due_day": 1,
        },
    )
    assert res.status_code == 409
    assert res.json()["error"]["code"] == "CONFLICT"


def test_existing_hostel_setting_remains_unchanged(transactional_client: TestClient, db_session: Session):
    """14. Existing HostelSetting remains unchanged after new Owner creation."""
    owner = create_test_owner(db_session, email="owner_setting_test@hostel.com")
    original_setting = db_session.query(HostelSetting).filter(HostelSetting.id == 1).first()
    assert original_setting is not None
    original_name = original_setting.hostel_name
    original_due_day = original_setting.rent_due_day
    original_owner_id = original_setting.owner_user_id

    headers = auth_headers(owner)
    res = transactional_client.post(
        "/api/v1/owners",
        json={
            "full_name": "New Co-Owner",
            "email": "coowner.setting@hostel.com",
            "password": "SettingPassword123",
        },
        headers=headers,
    )
    assert res.status_code == 201

    # Re-query setting
    db_session.expire_all()
    setting_after = db_session.query(HostelSetting).filter(HostelSetting.id == 1).first()
    assert setting_after is not None
    assert setting_after.hostel_name == original_name
    assert setting_after.rent_due_day == original_due_day
    assert setting_after.owner_user_id == original_owner_id
