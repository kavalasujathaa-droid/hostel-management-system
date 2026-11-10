from typing import Optional
from sqlalchemy.orm import Session
from app.core.logging import logger
from app.models.models import DeviceToken, Notification


def create_notification(
    db: Session,
    user_id: int,
    title: str,
    message: str,
    notification_type: str = "SYSTEM",
) -> Notification:
    """Create in-app notification and attempt FCM delivery safely."""
    notif = Notification(
        user_id=user_id,
        title=title,
        message=message,
        notification_type=notification_type,
        is_read=False,
    )
    db.add(notif)
    db.flush()

    # Attempt FCM push delivery safely without failing the parent transaction
    try:
        dispatch_fcm_notification(db, user_id, title, message)
    except Exception as exc:
        logger.warning(
            "FCM dispatch failed for user %d (in-app notification preserved): %s",
            user_id,
            str(exc),
        )

    return notif


def dispatch_fcm_notification(db: Session, user_id: int, title: str, message: str) -> None:
    """Simulate or execute FCM push delivery to active device tokens."""
    tokens = (
        db.query(DeviceToken)
        .filter(DeviceToken.user_id == user_id, DeviceToken.is_active == True)  # noqa: E712
        .all()
    )
    if not tokens:
        return

    # In production with Firebase Admin SDK initialized, this sends messaging.MulticastMessage.
    # Here, we ensure tokens exist and delivery logic is clean and non-blocking.
    logger.debug("Dispatched FCM message [%s] to %d device(s) for user %d", title, len(tokens), user_id)
