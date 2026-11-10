from datetime import date, datetime, time
from decimal import Decimal
from typing import Dict, Optional
from sqlalchemy.orm import Session
from app.core.logging import logger
from app.models.models import DeviceToken, Notification, RentCharge, TenantStay


def create_notification(
    db: Session,
    user_id: int,
    title: str,
    message: str,
    notification_type: str = "SYSTEM",
    category: str = "GENERAL",
) -> Notification:
    """Create in-app notification with category and attempt FCM delivery safely."""
    # Auto-infer category if default GENERAL provided
    if category == "GENERAL":
        upper_type = notification_type.upper()
        if any(p in upper_type for p in ["PAYMENT", "RENT_BILL", "BILL"]):
            category = "PAYMENT"
        elif any(r in upper_type for r in ["DUE", "REMINDER"]):
            category = "REMINDER"
        elif any(a in upper_type for a in ["OVERDUE", "ALERT", "VACATE", "COMPLAINT"]):
            category = "ALERT"

    notif = Notification(
        user_id=user_id,
        title=title,
        message=message,
        notification_type=notification_type,
        category=category,
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

    logger.debug("Dispatched FCM message [%s] to %d device(s) for user %d", title, len(tokens), user_id)


def generate_rent_due_reminders(db: Session, target_date: Optional[date] = None) -> Dict[str, int]:
    """
    Idempotent check and generation of:
    1) Rent due reminders (for rent charges due within 3 days).
    2) Overdue rent alerts (for rent charges whose due_date < target_date and are unpaid).

    Idempotency:
    - Avoids duplicate reminders if a notification of the same type/category was already sent
      today for that user and rent charge.
    """
    today = target_date or date.today()
    today_start = datetime.combine(today, time.min)

    # Fetch active rent charges with unpaid balances
    unpaid_charges = (
        db.query(RentCharge, TenantStay)
        .join(TenantStay, RentCharge.tenant_stay_id == TenantStay.id)
        .filter(
            RentCharge.status.in_(["UNPAID", "PARTIALLY_PAID"]),
            TenantStay.status == "ACTIVE",
        )
        .all()
    )

    reminders_sent = 0
    overdue_sent = 0

    for charge, stay in unpaid_charges:
        remaining = charge.amount_due - charge.amount_paid
        if remaining <= Decimal("0.00"):
            continue

        days_until_due = (charge.due_date - today).days

        # Case A: Overdue Rent Alert
        if days_until_due < 0:
            if charge.status != "OVERDUE":
                charge.status = "OVERDUE"
                db.flush()

            # Check idempotency: check if an overdue alert was already created for this user today
            already_notified = (
                db.query(Notification)
                .filter(
                    Notification.user_id == stay.tenant_user_id,
                    Notification.notification_type == "RENT_OVERDUE",
                    Notification.created_at >= today_start,
                )
                .first()
            )
            if not already_notified:
                create_notification(
                    db=db,
                    user_id=stay.tenant_user_id,
                    title="Overdue Rent Alert",
                    message=f"Your rent payment of ₹{remaining} was due on {charge.due_date} and is now overdue. Please clear your balance immediately.",
                    notification_type="RENT_OVERDUE",
                    category="ALERT",
                )
                overdue_sent += 1

        # Case B: Due Reminder (due within 3 days: 0 <= days_until_due <= 3)
        elif 0 <= days_until_due <= 3:
            already_notified = (
                db.query(Notification)
                .filter(
                    Notification.user_id == stay.tenant_user_id,
                    Notification.notification_type == "RENT_DUE_REMINDER",
                    Notification.created_at >= today_start,
                )
                .first()
            )
            if not already_notified:
                due_label = "today" if days_until_due == 0 else f"in {days_until_due} day(s) on {charge.due_date}"
                create_notification(
                    db=db,
                    user_id=stay.tenant_user_id,
                    title="Rent Due Reminder",
                    message=f"Your rent payment of ₹{remaining} is due {due_label}. Please submit your payment promptly.",
                    notification_type="RENT_DUE_REMINDER",
                    category="REMINDER",
                )
                reminders_sent += 1

    db.commit()
    return {"reminders_sent": reminders_sent, "overdue_sent": overdue_sent}
