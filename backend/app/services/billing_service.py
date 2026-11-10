import calendar
from datetime import date
from decimal import Decimal, ROUND_HALF_UP
from typing import List, Optional, Tuple
from sqlalchemy.orm import Session

from app.core.exceptions import ConflictError, NotFoundError, ValidationError
from app.models.models import HostelSetting, RentCharge, Room, TenantStay, User
from app.schemas.billing import RentChargeResponse
from app.services.audit_service import log_audit_event
from app.services.notification_service import create_notification


def calculate_rent(
    monthly_rent: Decimal,
    year: int,
    month: int,
    joining_date: date,
    approved_vacating_date: Optional[date] = None,
) -> Tuple[int, int, date, date, Decimal]:
    """
    Exact DECIMAL arithmetic for monthly rent calculation.
    Daily rent = Monthly rent / Number of calendar days in that month.
    Amount due = Daily rent * Billable days.
    Rounded using ROUND_HALF_UP quantized to 0.01.
    """
    days_in_month = calendar.monthrange(year, month)[1]
    month_start = date(year, month, 1)
    month_end = date(year, month, days_in_month)

    # If tenant joined after this month ends or vacated before this month starts:
    if joining_date > month_end:
        return days_in_month, 0, month_start, month_end, Decimal("0.00")
    if approved_vacating_date and approved_vacating_date < month_start:
        return days_in_month, 0, month_start, month_end, Decimal("0.00")

    # Effective period within the calendar month
    period_start = max(month_start, joining_date)
    if approved_vacating_date and approved_vacating_date < month_end:
        period_end = approved_vacating_date
    else:
        period_end = month_end

    if period_start > period_end:
        return days_in_month, 0, month_start, month_end, Decimal("0.00")

    # Joining date and vacating date are both inclusive/billable
    billable_days = (period_end - period_start).days + 1
    billable_days = max(0, min(billable_days, days_in_month))

    d_monthly = Decimal(str(monthly_rent))
    d_total_days = Decimal(days_in_month)
    d_billable_days = Decimal(billable_days)

    if billable_days == days_in_month:
        amount_due = d_monthly.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
    elif billable_days == 0:
        amount_due = Decimal("0.00")
    else:
        # Standard formula: (Monthly rent / days_in_month) * billable_days
        daily_rent = d_monthly / d_total_days
        amount_due = (daily_rent * d_billable_days).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

    return days_in_month, billable_days, period_start, period_end, amount_due


def generate_monthly_billing(
    db: Session,
    year: int,
    month: int,
    actor_user_id: int,
) -> List[RentChargeResponse]:
    """
    Generate rent charges for all eligible active tenant stays for a given month/year.
    Enforces singleton due date and prevents duplicate billing records.
    """
    hostel_setting = db.query(HostelSetting).filter(HostelSetting.id == 1).first()
    due_day = hostel_setting.rent_due_day if hostel_setting else 5

    days_in_month = calendar.monthrange(year, month)[1]
    safe_due_day = min(due_day, days_in_month)
    due_date = date(year, month, safe_due_day)
    month_start = date(year, month, 1)

    # Query all stays active in or prior to this month
    active_stays = (
        db.query(TenantStay)
        .filter(
            TenantStay.status == "ACTIVE",
            TenantStay.joining_date <= date(year, month, days_in_month),
        )
        .all()
    )

    generated_charges = []

    for stay in active_stays:
        # Prevent duplicate monthly billing (uq_rent_stay_billing_period)
        existing = (
            db.query(RentCharge)
            .filter(
                RentCharge.tenant_stay_id == stay.id,
                RentCharge.billing_year == year,
                RentCharge.billing_month == month,
            )
            .first()
        )
        if existing:
            continue

        days_in_m, billable_d, p_start, p_end, amount_due = calculate_rent(
            monthly_rent=stay.monthly_rent,
            year=year,
            month=month,
            joining_date=stay.joining_date,
            approved_vacating_date=stay.approved_vacating_date,
        )

        # Do not generate 0-day charges if stay doesn't overlap
        if billable_d == 0:
            continue

        charge = RentCharge(
            tenant_stay_id=stay.id,
            billing_year=year,
            billing_month=month,
            period_start=p_start,
            period_end=p_end,
            monthly_rent_snapshot=stay.monthly_rent,
            days_in_month=days_in_m,
            billable_days=billable_d,
            amount_due=amount_due,
            amount_paid=Decimal("0.00"),
            due_date=due_date,
            status="UNPAID",
        )
        db.add(charge)
        db.flush()

        # Send in-app notification to tenant
        create_notification(
            db=db,
            user_id=stay.tenant_user_id,
            title="Monthly Rent Bill Generated",
            message=f"Rent bill of ₹{amount_due} for {calendar.month_name[month]} {year} is due by {due_date}.",
            notification_type="RENT_BILL",
        )
        generated_charges.append(charge)

    log_audit_event(
        db=db,
        action="MONTHLY_BILLING_GENERATED",
        entity_type="RENT_CHARGE",
        actor_user_id=actor_user_id,
        details={"year": year, "month": month, "count": len(generated_charges)},
    )
    db.commit()

    return list_rent_charges(db, year=year, month=month)


def list_rent_charges(
    db: Session,
    tenant_id: Optional[int] = None,
    year: Optional[int] = None,
    month: Optional[int] = None,
    status: Optional[str] = None,
) -> List[RentChargeResponse]:
    """Query rent charges with balances and user/room metadata."""
    query = (
        db.query(RentCharge, TenantStay, User, Room)
        .join(TenantStay, RentCharge.tenant_stay_id == TenantStay.id)
        .join(User, TenantStay.tenant_user_id == User.id)
        .join(Room, TenantStay.room_id == Room.id)
    )

    if tenant_id is not None:
        query = query.filter(TenantStay.tenant_user_id == tenant_id)
    if year is not None:
        query = query.filter(RentCharge.billing_year == year)
    if month is not None:
        query = query.filter(RentCharge.billing_month == month)
    if status is not None:
        query = query.filter(RentCharge.status == status)

    today = date.today()
    rows = query.order_by(RentCharge.billing_year.desc(), RentCharge.billing_month.desc()).all()

    results = []
    for charge, stay, user, room in rows:
        # Check overdue dynamically
        current_status = charge.status
        if current_status in ("UNPAID", "PARTIALLY_PAID") and charge.due_date < today:
            current_status = "OVERDUE"

        balance = charge.amount_due - charge.amount_paid
        results.append(
            RentChargeResponse(
                id=charge.id,
                tenant_stay_id=charge.tenant_stay_id,
                tenant_name=user.full_name,
                room_number=room.room_number,
                billing_year=charge.billing_year,
                billing_month=charge.billing_month,
                period_start=charge.period_start,
                period_end=charge.period_end,
                monthly_rent_snapshot=charge.monthly_rent_snapshot,
                days_in_month=charge.days_in_month,
                billable_days=charge.billable_days,
                amount_due=charge.amount_due,
                amount_paid=charge.amount_paid,
                balance=balance,
                due_date=charge.due_date,
                status=current_status,
                created_at=charge.created_at,
            )
        )
    return results


def get_rent_charge_by_id(db: Session, charge_id: int) -> RentChargeResponse:
    """Retrieve details for a single rent charge."""
    row = (
        db.query(RentCharge, TenantStay, User, Room)
        .join(TenantStay, RentCharge.tenant_stay_id == TenantStay.id)
        .join(User, TenantStay.tenant_user_id == User.id)
        .join(Room, TenantStay.room_id == Room.id)
        .filter(RentCharge.id == charge_id)
        .first()
    )
    if not row:
        raise NotFoundError(f"Rent charge with id {charge_id} not found")

    charge, stay, user, room = row
    balance = charge.amount_due - charge.amount_paid
    return RentChargeResponse(
        id=charge.id,
        tenant_stay_id=charge.tenant_stay_id,
        tenant_name=user.full_name,
        room_number=room.room_number,
        billing_year=charge.billing_year,
        billing_month=charge.billing_month,
        period_start=charge.period_start,
        period_end=charge.period_end,
        monthly_rent_snapshot=charge.monthly_rent_snapshot,
        days_in_month=charge.days_in_month,
        billable_days=charge.billable_days,
        amount_due=charge.amount_due,
        amount_paid=charge.amount_paid,
        balance=balance,
        due_date=charge.due_date,
        status=charge.status,
        created_at=charge.created_at,
    )
