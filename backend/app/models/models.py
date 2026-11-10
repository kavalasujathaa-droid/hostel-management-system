
from datetime import date, datetime
from decimal import Decimal

from sqlalchemy import (
    BigInteger,
    Boolean,
    CheckConstraint,
    Date,
    DateTime,
    ForeignKey,
    Index,
    Integer,
    JSON,
    Numeric,
    String,
    Text,
    UniqueConstraint,
    func,
    text,
)
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base


# 1. USERS: Owner and tenant authentication
class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    full_name: Mapped[str] = mapped_column(String(120), nullable=False)
    email: Mapped[str] = mapped_column(String(255), nullable=False, unique=True)
    password_hash: Mapped[str] = mapped_column(String(255), nullable=False)
    role: Mapped[str] = mapped_column(String(20), nullable=False)
    phone: Mapped[str | None] = mapped_column(String(20), nullable=True)
    is_active: Mapped[bool] = mapped_column(Boolean, nullable=False, server_default=text("1"))
    created_at: Mapped[DateTime] = mapped_column(DateTime, nullable=False, server_default=func.now())
    updated_at: Mapped[DateTime] = mapped_column(
        DateTime, nullable=False, server_default=func.now(), onupdate=func.now()
    )

    __table_args__ = (
        CheckConstraint("role IN ('OWNER', 'TENANT')", name="ck_users_role"),
        Index("ix_users_role_active", "role", "is_active"),
    )


# 2. ROOMS: Dynamic room management
class Room(Base):
    __tablename__ = "rooms"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    room_number: Mapped[str] = mapped_column(String(30), nullable=False, unique=True)
    floor: Mapped[str | None] = mapped_column(String(30), nullable=True)
    capacity: Mapped[int] = mapped_column(Integer, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, nullable=False, server_default=text("1"))
    created_at: Mapped[DateTime] = mapped_column(DateTime, nullable=False, server_default=func.now())
    updated_at: Mapped[DateTime] = mapped_column(
        DateTime, nullable=False, server_default=func.now(), onupdate=func.now()
    )

    __table_args__ = (
        CheckConstraint("capacity > 0", name="ck_rooms_capacity_positive"),
        Index("ix_rooms_active", "is_active"),
    )


# 3. TENANT STAYS: Room assignments and historical stays
class TenantStay(Base):
    __tablename__ = "tenant_stays"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    tenant_user_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("users.id", ondelete="RESTRICT"), nullable=False
    )
    room_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("rooms.id", ondelete="RESTRICT"), nullable=False
    )
    joining_date: Mapped[date] = mapped_column(Date, nullable=False)
    approved_vacating_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    monthly_rent: Mapped[Decimal] = mapped_column(Numeric(10, 2), nullable=False)
    status: Mapped[str] = mapped_column(
        String(20), nullable=False, server_default=text("'ACTIVE'")
    )
    created_at: Mapped[DateTime] = mapped_column(DateTime, nullable=False, server_default=func.now())
    updated_at: Mapped[DateTime] = mapped_column(
        DateTime, nullable=False, server_default=func.now(), onupdate=func.now()
    )

    __table_args__ = (
        CheckConstraint("monthly_rent >= 0", name="ck_stays_rent_nonnegative"),
        CheckConstraint("status IN ('ACTIVE', 'COMPLETED')", name="ck_stays_status"),
        CheckConstraint(
            "approved_vacating_date IS NULL OR approved_vacating_date >= joining_date",
            name="ck_stays_vacating_date",
        ),
        Index("ix_stays_tenant_status", "tenant_user_id", "status"),
        Index("ix_stays_room_status", "room_id", "status"),
    )


# 4. RENT CHARGES: Monthly billing and rent snapshots
class RentCharge(Base):
    __tablename__ = "rent_charges"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    tenant_stay_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("tenant_stays.id", ondelete="RESTRICT"), nullable=False
    )
    billing_year: Mapped[int] = mapped_column(Integer, nullable=False)
    billing_month: Mapped[int] = mapped_column(Integer, nullable=False)
    period_start: Mapped[date] = mapped_column(Date, nullable=False)
    period_end: Mapped[date] = mapped_column(Date, nullable=False)
    monthly_rent_snapshot: Mapped[Decimal] = mapped_column(Numeric(10, 2), nullable=False)
    days_in_month: Mapped[int] = mapped_column(Integer, nullable=False)
    billable_days: Mapped[int] = mapped_column(Integer, nullable=False)
    amount_due: Mapped[Decimal] = mapped_column(Numeric(10, 2), nullable=False)
    amount_paid: Mapped[Decimal] = mapped_column(
        Numeric(10, 2), nullable=False, server_default=text("0.00")
    )
    due_date: Mapped[date] = mapped_column(Date, nullable=False)
    status: Mapped[str] = mapped_column(
        String(20), nullable=False, server_default=text("'UNPAID'")
    )
    created_at: Mapped[DateTime] = mapped_column(DateTime, nullable=False, server_default=func.now())
    updated_at: Mapped[DateTime] = mapped_column(
        DateTime, nullable=False, server_default=func.now(), onupdate=func.now()
    )

    __table_args__ = (
        UniqueConstraint(
            "tenant_stay_id", "billing_year", "billing_month",
            name="uq_rent_stay_billing_period",
        ),
        CheckConstraint("billing_month BETWEEN 1 AND 12", name="ck_rent_billing_month"),
        CheckConstraint("days_in_month BETWEEN 28 AND 31", name="ck_rent_days_in_month"),
        CheckConstraint("billable_days BETWEEN 0 AND 31", name="ck_rent_billable_days"),
        CheckConstraint("monthly_rent_snapshot >= 0", name="ck_rent_snapshot_nonnegative"),
        CheckConstraint("amount_due >= 0", name="ck_rent_due_nonnegative"),
        CheckConstraint("amount_paid >= 0", name="ck_rent_paid_nonnegative"),
        CheckConstraint("amount_paid <= amount_due", name="ck_rent_no_overpayment"),
        CheckConstraint("billable_days <= days_in_month", name="ck_rent_billable_within_month"),
        CheckConstraint("period_start <= period_end", name="ck_rent_valid_period"),
        CheckConstraint(
            "status IN ('UNPAID', 'PARTIALLY_PAID', 'PAID', 'OVERDUE')",
            name="ck_rent_status",
        ),
        Index("ix_rent_status_due_date", "status", "due_date"),
    )


# 5. PAYMENTS: Tenant payment submissions and owner verification
class Payment(Base):
    __tablename__ = "payments"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    rent_charge_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("rent_charges.id", ondelete="RESTRICT"), nullable=False
    )
    submitted_by: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("users.id", ondelete="RESTRICT"), nullable=False
    )
    reviewed_by: Mapped[int | None] = mapped_column(
        BigInteger, ForeignKey("users.id", ondelete="RESTRICT"), nullable=True
    )
    amount: Mapped[Decimal] = mapped_column(Numeric(10, 2), nullable=False)
    utr: Mapped[str] = mapped_column(String(100), nullable=False, unique=True)
    status: Mapped[str] = mapped_column(
        String(20), nullable=False, server_default=text("'PENDING'")
    )
    rejection_reason: Mapped[str | None] = mapped_column(Text, nullable=True)
    proof_image_path: Mapped[str | None] = mapped_column(String(500), nullable=True)
    submitted_at: Mapped[DateTime] = mapped_column(DateTime, nullable=False, server_default=func.now())
    reviewed_at: Mapped[DateTime | None] = mapped_column(DateTime, nullable=True)

    __table_args__ = (
        CheckConstraint("amount > 0", name="ck_payments_amount_positive"),
        CheckConstraint(
            "status IN ('PENDING', 'APPROVED', 'REJECTED')",
            name="ck_payments_status",
        ),
        Index("ix_payments_rent_status", "rent_charge_id", "status"),
        Index("ix_payments_submitted_by", "submitted_by"),
    )


# 6. PAYMENT QR: Owner's active UPI QR image
class PaymentQR(Base):
    __tablename__ = "payment_qr"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    uploaded_by: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("users.id", ondelete="RESTRICT"), nullable=False
    )
    image_path: Mapped[str] = mapped_column(String(500), nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, nullable=False, server_default=text("1"))
    created_at: Mapped[DateTime] = mapped_column(DateTime, nullable=False, server_default=func.now())

    __table_args__ = (Index("ix_payment_qr_active", "is_active"),)


# 7. COMPLAINTS: Text-only tenant complaints
class Complaint(Base):
    __tablename__ = "complaints"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    tenant_stay_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("tenant_stays.id", ondelete="RESTRICT"), nullable=False
    )
    description: Mapped[str] = mapped_column(Text, nullable=False)
    status: Mapped[str] = mapped_column(
        String(20), nullable=False, server_default=text("'OPEN'")
    )
    created_at: Mapped[DateTime] = mapped_column(DateTime, nullable=False, server_default=func.now())
    updated_at: Mapped[DateTime] = mapped_column(
        DateTime, nullable=False, server_default=func.now(), onupdate=func.now()
    )

    __table_args__ = (
        CheckConstraint(
            "status IN ('OPEN', 'IN PROGRESS')",
            name="ck_complaints_status",
        ),
        Index("ix_complaints_status_created", "status", "created_at"),
    )


# 8. VACATE REQUESTS: Tenant vacating workflow
class VacateRequest(Base):
    __tablename__ = "vacate_requests"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    tenant_stay_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("tenant_stays.id", ondelete="RESTRICT"), nullable=False
    )
    requested_vacating_date: Mapped[date] = mapped_column(Date, nullable=False)
    status: Mapped[str] = mapped_column(
        String(20), nullable=False, server_default=text("'PENDING'")
    )
    reviewed_by: Mapped[int | None] = mapped_column(
        BigInteger, ForeignKey("users.id", ondelete="RESTRICT"), nullable=True
    )
    rejection_reason: Mapped[str | None] = mapped_column(Text, nullable=True)
    requested_at: Mapped[DateTime] = mapped_column(DateTime, nullable=False, server_default=func.now())
    reviewed_at: Mapped[DateTime | None] = mapped_column(DateTime, nullable=True)

    __table_args__ = (
        CheckConstraint(
            "status IN ('PENDING', 'APPROVED', 'REJECTED')",
            name="ck_vacate_status",
        ),
        Index("ix_vacate_status", "status"),
    )


# 9. NOTIFICATIONS: In-app notification history
class Notification(Base):
    __tablename__ = "notifications"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("users.id", ondelete="CASCADE"), nullable=False
    )
    title: Mapped[str] = mapped_column(String(150), nullable=False)
    message: Mapped[str] = mapped_column(Text, nullable=False)
    notification_type: Mapped[str] = mapped_column(String(50), nullable=False)
    category: Mapped[str] = mapped_column(String(50), nullable=False, server_default=text("'GENERAL'"))
    is_read: Mapped[bool] = mapped_column(Boolean, nullable=False, server_default=text("0"))
    created_at: Mapped[DateTime] = mapped_column(DateTime, nullable=False, server_default=func.now())

    __table_args__ = (Index("ix_notifications_user_read", "user_id", "is_read"),)


# 10. DEVICE TOKENS: Firebase Cloud Messaging tokens
class DeviceToken(Base):
    __tablename__ = "device_tokens"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("users.id", ondelete="CASCADE"), nullable=False
    )
    fcm_token: Mapped[str] = mapped_column(String(512), nullable=False, unique=True)
    platform: Mapped[str] = mapped_column(String(20), nullable=False, server_default=text("'ANDROID'"))
    is_active: Mapped[bool] = mapped_column(Boolean, nullable=False, server_default=text("1"))
    updated_at: Mapped[DateTime] = mapped_column(
        DateTime, nullable=False, server_default=func.now(), onupdate=func.now()
    )

    __table_args__ = (
        CheckConstraint("platform IN ('ANDROID')", name="ck_device_platform"),
        Index("ix_device_user_active", "user_id", "is_active"),
    )


# 11. AUDIT LOGS: Important system activity history
class AuditLog(Base):
    __tablename__ = "audit_logs"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    actor_user_id: Mapped[int | None] = mapped_column(
        BigInteger, ForeignKey("users.id", ondelete="SET NULL"), nullable=True
    )
    action: Mapped[str] = mapped_column(String(100), nullable=False)
    entity_type: Mapped[str] = mapped_column(String(80), nullable=False)
    entity_id: Mapped[int | None] = mapped_column(BigInteger, nullable=True)
    details: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    created_at: Mapped[DateTime] = mapped_column(DateTime, nullable=False, server_default=func.now())

    __table_args__ = (
        Index("ix_audit_entity", "entity_type", "entity_id"),
        Index("ix_audit_actor_created", "actor_user_id", "created_at"),
    )


# 12. HOSTEL SETTINGS: Single-hostel configuration
class HostelSetting(Base):
    __tablename__ = "hostel_settings"

    id: Mapped[int] = mapped_column(
        Integer,
        primary_key=True,
        default=1,
        server_default=text("1"),
    )
    owner_user_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("users.id", ondelete="RESTRICT"), nullable=False, unique=True
    )
    hostel_name: Mapped[str] = mapped_column(String(150), nullable=False)
    address: Mapped[str | None] = mapped_column(Text, nullable=True)
    rent_due_day: Mapped[int] = mapped_column(Integer, nullable=False, server_default=text("5"))
    currency: Mapped[str] = mapped_column(String(3), nullable=False, server_default=text("'INR'"))
    updated_at: Mapped[DateTime] = mapped_column(
        DateTime, nullable=False, server_default=func.now(), onupdate=func.now()
    )

    __table_args__ = (
        CheckConstraint("id = 1", name="ck_hostel_singleton"),
        CheckConstraint("rent_due_day BETWEEN 1 AND 28", name="ck_hostel_due_day"),
        CheckConstraint("currency = 'INR'", name="ck_hostel_currency"),
    )


# 13. REFRESH TOKENS: Database-backed refresh-session storage
class RefreshToken(Base):
    __tablename__ = "refresh_tokens"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(
        BigInteger, ForeignKey("users.id", ondelete="CASCADE"), nullable=False
    )
    token_hash: Mapped[str] = mapped_column(String(128), nullable=False, unique=True)
    device_info: Mapped[str | None] = mapped_column(String(255), nullable=True)
    ip_address: Mapped[str | None] = mapped_column(String(45), nullable=True)
    expires_at: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    is_revoked: Mapped[bool] = mapped_column(Boolean, nullable=False, server_default=text("0"))
    created_at: Mapped[datetime] = mapped_column(DateTime, nullable=False, server_default=func.now())
    revoked_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)

    __table_args__ = (
        Index("ix_refresh_tokens_user_id", "user_id"),
        Index("ix_refresh_tokens_hash", "token_hash"),
    )

