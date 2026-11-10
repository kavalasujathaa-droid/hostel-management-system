from typing import Generator, Optional
from sqlalchemy import create_engine, text, URL
from sqlalchemy.orm import Session, sessionmaker

from app.core.config import settings
from app.core.logging import logger
from app.db.base import Base

database_url = URL.create(
    drivername="mysql+pymysql",
    username=settings.TIDB_USER,
    password=settings.TIDB_PASSWORD,
    host=settings.TIDB_HOST,
    port=settings.TIDB_PORT,
    database=settings.TIDB_DATABASE,
    query={
        "ssl_verify_cert": "true",
        "ssl_verify_identity": "true",
    },
)

engine = create_engine(
    database_url,
    pool_pre_ping=True,
    pool_recycle=300,
    pool_size=3,
    max_overflow=2,
    connect_args={"connect_timeout": 15},
)

SessionLocal = sessionmaker(
    bind=engine,
    autoflush=False,
    autocommit=False,
)


def get_db() -> Generator[Session, None, None]:
    """Provide a database session lifecycle for FastAPI dependency injection."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def check_db_connectivity(db: Optional[Session] = None) -> bool:
    """
    Safely verify database connectivity with SELECT 1.
    Catches all connectivity exceptions and suppresses credential leaks.
    """
    try:
        if db is not None:
            db.execute(text("SELECT 1"))
        else:
            with engine.connect() as conn:
                conn.execute(text("SELECT 1"))
        return True
    except Exception as exc:
        logger.error("Database connectivity ping failed: %s", type(exc).__name__)
        return False
