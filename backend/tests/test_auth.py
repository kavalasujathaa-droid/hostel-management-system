from datetime import datetime, timedelta, timezone
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.core.security import (
    create_access_token,
    decode_access_token,
    generate_refresh_token,
    hash_password,
    hash_refresh_token,
    verify_password,
)
from app.models.models import RefreshToken, User
from tests.conftest import auth_headers, create_test_owner, create_test_tenant


def test_argon2id_password_hashing():
    """Verify Argon2id password hashing and constant-time verification."""
    password = "SuperSecretPassword123!"
    hashed = hash_password(password)

    assert hashed != password
    assert "$argon2id$" in hashed
    assert verify_password(password, hashed) is True
    assert verify_password("WrongPassword123!", hashed) is False


def test_owner_initialization_bootstrap(transactional_client: TestClient, db_session: Session):
    """Test one-time owner bootstrap initialization and duplicate block."""
    from app.models.models import HostelSetting, User
    db_session.query(HostelSetting).delete()
    db_session.query(User).filter(User.role == "OWNER").delete()
    db_session.flush()

    payload = {
        "full_name": "First Owner",
        "email": "first_owner@hostel.com",
        "password": "SecureOwnerPassword2026!",
        "phone": "9876543210",
        "hostel_name": "Royal Palms Hostel",
        "address": "456 Palace Road",
        "rent_due_day": 5,
    }

    # First attempt: succeeds
    res1 = transactional_client.post("/api/v1/auth/init-owner", json=payload)
    assert res1.status_code == 201
    data = res1.json()
    assert "access_token" in data
    assert "refresh_token" in data
    assert data["user"]["role"] == "OWNER"

    # Second attempt: blocked with 409 Conflict
    res2 = transactional_client.post("/api/v1/auth/init-owner", json=payload)
    assert res2.status_code == 409
    assert res2.json()["error"]["code"] == "CONFLICT"


def test_login_success_and_failure(transactional_client: TestClient, db_session: Session):
    """Test user login with valid and invalid credentials."""
    owner = create_test_owner(db_session, email="login_owner@test.com")

    # Valid login
    res = transactional_client.post(
        "/api/v1/auth/login",
        json={"email": "login_owner@test.com", "password": "OwnerPassword123"},
    )
    assert res.status_code == 200
    data = res.json()
    assert data["user"]["email"] == "login_owner@test.com"
    assert "access_token" in data

    # Invalid password
    res_wrong = transactional_client.post(
        "/api/v1/auth/login",
        json={"email": "login_owner@test.com", "password": "WrongPassword"},
    )
    assert res_wrong.status_code == 401

    # Inactive user blocked
    owner.is_active = False
    db_session.flush()
    res_inactive = transactional_client.post(
        "/api/v1/auth/login",
        json={"email": "login_owner@test.com", "password": "OwnerPassword123"},
    )
    assert res_inactive.status_code == 403


def test_jwt_access_token_claims_and_expiry():
    """Verify JWT access token contains required claims and honors expiration."""
    token = create_access_token(
        subject=101,
        role="TENANT",
        email="tenant@example.com",
        expires_delta=timedelta(minutes=15),
    )
    claims = decode_access_token(token)
    assert claims["sub"] == "101"
    assert claims["role"] == "TENANT"
    assert claims["email"] == "tenant@example.com"
    assert "exp" in claims


def test_refresh_token_rotation_and_replay_detection(transactional_client: TestClient, db_session: Session):
    """Test database-backed refresh token rotation and RFC 6819 replay attack mitigation."""
    tenant = create_test_tenant(db_session, email="refresh_tenant@test.com")

    # Login to obtain refresh token
    login_res = transactional_client.post(
        "/api/v1/auth/login",
        json={"email": "refresh_tenant@test.com", "password": "TenantPassword123"},
    )
    initial_refresh = login_res.json()["refresh_token"]

    # 1. Successful rotation
    rotate_res = transactional_client.post(
        "/api/v1/auth/refresh",
        json={"refresh_token": initial_refresh},
    )
    assert rotate_res.status_code == 200
    new_refresh = rotate_res.json()["refresh_token"]
    assert new_refresh != initial_refresh

    # 2. Replay attack: submitting the already-rotated old refresh token must be detected
    replay_res = transactional_client.post(
        "/api/v1/auth/refresh",
        json={"refresh_token": initial_refresh},
    )
    assert replay_res.status_code == 401
    assert "replayed" in replay_res.json()["error"]["message"].lower()

    # Verify that all active tokens for this user were revoked as a security measure
    subsequent_res = transactional_client.post(
        "/api/v1/auth/refresh",
        json={"refresh_token": new_refresh},
    )
    assert subsequent_res.status_code == 401


def test_logout_and_password_change(transactional_client: TestClient, db_session: Session):
    """Test logout token invalidation and password change workflow."""
    tenant = create_test_tenant(db_session, email="logout_tenant@test.com")
    headers = auth_headers(tenant)

    # Login to obtain a refresh token
    login_res = transactional_client.post(
        "/api/v1/auth/login",
        json={"email": "logout_tenant@test.com", "password": "TenantPassword123"},
    )
    refresh_token = login_res.json()["refresh_token"]

    # Logout
    logout_res = transactional_client.post(
        "/api/v1/auth/logout",
        json={"refresh_token": refresh_token},
        headers=headers,
    )
    assert logout_res.status_code == 200

    # Ensure refresh token is now revoked
    revoked_check = transactional_client.post(
        "/api/v1/auth/refresh",
        json={"refresh_token": refresh_token},
    )
    assert revoked_check.status_code == 401

    # Change password
    pwd_res = transactional_client.put(
        "/api/v1/users/me/password",
        json={"old_password": "TenantPassword123", "new_password": "NewSecurePassword456"},
        headers=headers,
    )
    assert pwd_res.status_code == 200

    # Verify login with new password works and old password fails
    fail_res = transactional_client.post(
        "/api/v1/auth/login",
        json={"email": "logout_tenant@test.com", "password": "TenantPassword123"},
    )
    assert fail_res.status_code == 401

    success_res = transactional_client.post(
        "/api/v1/auth/login",
        json={"email": "logout_tenant@test.com", "password": "NewSecurePassword456"},
    )
    assert success_res.status_code == 200
