from typing import List, Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

from app.core.dependencies import get_current_user, require_owner
from app.core.exceptions import NotFoundError
from app.db.session import get_db
from app.models.models import Notification, User
from app.schemas.notification import NotificationResponse
from app.services.notification_service import generate_rent_due_reminders

router = APIRouter()


@router.get("", response_model=List[NotificationResponse], status_code=status.HTTP_200_OK)
def list_my_notifications(
    category: Optional[str] = Query(None, description="Filter by category: ALL, PAYMENT, REMINDER, ALERT, GENERAL"),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> List[NotificationResponse]:
    """Retrieve notifications for the current user, optionally filtered by category."""
    query = db.query(Notification).filter(Notification.user_id == current_user.id)

    if category and category.upper() != "ALL":
        # Handle plural vs singular aliases gracefully (e.g. PAYMENTS -> PAYMENT)
        clean_category = category.upper()
        if clean_category.endswith("S"):
            clean_category = clean_category[:-1]
        query = query.filter(Notification.category == clean_category)

    notifs = query.order_by(Notification.created_at.desc()).limit(100).all()
    return [NotificationResponse.model_validate(n) for n in notifs]


@router.post("/trigger-reminders", status_code=status.HTTP_200_OK)
def trigger_rent_reminders(
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
):
    """
    Owner or cron endpoint to generate rent due reminders and overdue alerts idempotently.
    """
    result = generate_rent_due_reminders(db=db)
    return {
        "success": True,
        "message": "Rent reminders and overdue alerts processed successfully",
        "result": result,
    }


@router.put("/{notification_id}/read", status_code=status.HTTP_200_OK)
def mark_notification_as_read(
    notification_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """Mark a notification as read."""
    notif = (
        db.query(Notification)
        .filter(Notification.id == notification_id, Notification.user_id == current_user.id)
        .first()
    )
    if not notif:
        raise NotFoundError(f"Notification with id {notification_id} not found")

    notif.is_read = True
    db.commit()
    return {"success": True, "message": "Notification marked as read"}


@router.put("/read-all", status_code=status.HTTP_200_OK)
def mark_all_notifications_as_read(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    """Mark all notifications as read for current user."""
    db.query(Notification).filter(
        Notification.user_id == current_user.id,
        Notification.is_read == False,  # noqa: E712
    ).update({"is_read": True})
    db.commit()
    return {"success": True, "message": "All notifications marked as read"}
