import pytest
from typing import Generator
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.core.config import Settings
from app.core.security import create_access_token, hash_password
from app.db.session import engine, get_db
from app.main import app
from app.models.models import HostelSetting, User


@pytest.fixture(scope="session")
def client() -> TestClient:
    """FastAPI TestClient fixture."""
    with TestClient(app) as test_client:
        yield test_client


@pytest.fixture
def mock_settings() -> Settings:
    """Fixture providing isolated Settings instance with dummy credentials."""
    return Settings(
        APP_NAME="Hostel Test App",
        APP_ENV="test",
        DEBUG=True,
        API_V1_PREFIX="/api/v1",
        CORS_ORIGINS=["http://testserver"],
        TIDB_HOST="test-host",
        TIDB_PORT=4000,
        TIDB_USER="test-user",
        TIDB_PASSWORD="test-password",
        TIDB_DATABASE="test-db",
    )


@pytest.fixture
def db_session() -> Generator[Session, None, None]:
    """
    Transactional database session that automatically rolls back all operations
    after each test. Protects production data against persistent modifications.
    """
    connection = engine.connect()
    transaction = connection.begin()
    session = Session(bind=connection, expire_on_commit=False)

    yield session

    session.close()
    transaction.rollback()
    connection.close()


@pytest.fixture
def transactional_client(db_session: Session) -> Generator[TestClient, None, None]:
    """TestClient wired with a transactional database session that rolls back on exit."""
    def override_get_db():
        try:
            yield db_session
        finally:
            pass

    app.dependency_overrides[get_db] = override_get_db
    with TestClient(app) as test_client:
        yield test_client
    app.dependency_overrides.pop(get_db, None)


def create_test_owner(db: Session, email: str = "owner@test.com") -> User:
    """Helper to create and return an OWNER user."""
    owner = User(
        full_name="Test Owner",
        email=email,
        password_hash=hash_password("OwnerPassword123"),
        role="OWNER",
        phone="9999999999",
        is_active=True,
    )
    db.add(owner)
    db.flush()

    # Ensure hostel_settings exists if not already
    setting = db.query(HostelSetting).filter(HostelSetting.id == 1).first()
    if not setting:
        setting = HostelSetting(
            id=1,
            owner_user_id=owner.id,
            hostel_name="Grand Test Hostel",
            address="123 Test Street",
            rent_due_day=5,
            currency="INR",
        )
        db.add(setting)
        db.flush()

    return owner


def create_test_tenant(db: Session, email: str = "tenant@test.com") -> User:
    """Helper to create and return a TENANT user."""
    tenant = User(
        full_name="Test Tenant",
        email=email,
        password_hash=hash_password("TenantPassword123"),
        role="TENANT",
        phone="8888888888",
        is_active=True,
    )
    db.add(tenant)
    db.flush()
    return tenant


def auth_headers(user: User) -> dict:
    """Generate authorization Bearer headers for given user."""
    token = create_access_token(subject=user.id, role=user.role, email=user.email)
    return {"Authorization": f"Bearer {token}"}
