from unittest.mock import MagicMock
from sqlalchemy.exc import OperationalError
from app.core.config import Settings
from app.core.exceptions import DatabaseConnectionError
from app.db.session import check_db_connectivity


def test_check_db_connectivity_success():
    """Verify check_db_connectivity returns True when execution succeeds."""
    mock_db = MagicMock()
    mock_db.execute.return_value = None
    assert check_db_connectivity(mock_db) is True
    mock_db.execute.assert_called_once()


def test_check_db_connectivity_failure_handled_gracefully():
    """Verify check_db_connectivity safely catches OperationalError and returns False."""
    mock_db = MagicMock()
    mock_db.execute.side_effect = OperationalError("Access denied for user 'root'@'host'", params=None, orig=Exception())
    
    result = check_db_connectivity(mock_db)
    assert result is False


def test_database_connection_error_attributes():
    """Verify DatabaseConnectionError attributes and HTTP status code."""
    exc = DatabaseConnectionError("Custom database error")
    assert exc.status_code == 503
    assert exc.code == "DATABASE_UNAVAILABLE"
    assert exc.message == "Custom database error"


def test_configuration_loading_defaults_and_parsing():
    """Verify Settings loads properly and parses various CORS configurations."""
    # Comma-separated CORS string
    s1 = Settings(
        TIDB_HOST="test",
        TIDB_USER="user",
        TIDB_PASSWORD="pwd",
        CORS_ORIGINS="http://localhost:3000, http://localhost:8080",
    )
    assert s1.CORS_ORIGINS == ["http://localhost:3000", "http://localhost:8080"]

    # JSON array CORS string
    s2 = Settings(
        TIDB_HOST="test",
        TIDB_USER="user",
        TIDB_PASSWORD="pwd",
        CORS_ORIGINS='["http://example.com", "http://app.local"]',
    )
    assert s2.CORS_ORIGINS == ["http://example.com", "http://app.local"]

    # List input
    s3 = Settings(
        TIDB_HOST="test",
        TIDB_USER="user",
        TIDB_PASSWORD="pwd",
        CORS_ORIGINS=["http://localhost"],
    )
    assert s3.CORS_ORIGINS == ["http://localhost"]
