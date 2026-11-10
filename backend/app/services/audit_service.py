from typing import Any, Dict, Optional
from sqlalchemy.orm import Session
from app.models.models import AuditLog


def log_audit_event(
    db: Session,
    action: str,
    entity_type: str,
    entity_id: Optional[int] = None,
    actor_user_id: Optional[int] = None,
    details: Optional[Dict[str, Any]] = None,
) -> AuditLog:
    """Record an audit trail event."""
    log_entry = AuditLog(
        action=action,
        entity_type=entity_type,
        entity_id=entity_id,
        actor_user_id=actor_user_id,
        details=details,
    )
    db.add(log_entry)
    return log_entry
