from decimal import Decimal
from typing import List, Optional
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.models.models import Complaint, Payment, RentCharge, Room, TenantStay, VacateRequest
from app.schemas.report import DashboardStats, FinancialReport, MonthlyFinancialRecord


def get_dashboard_stats(db: Session) -> DashboardStats:
    """Compute real-time operational dashboard metrics for the hostel owner."""
    total_rooms = db.query(func.count(Room.id)).filter(Room.is_active == True).scalar() or 0  # noqa: E712
    total_capacity = (
        db.query(func.coalesce(func.sum(Room.capacity), 0)).filter(Room.is_active == True).scalar() or 0  # noqa: E712
    )

    total_active_tenants = (
        db.query(func.count(TenantStay.id)).filter(TenantStay.status == "ACTIVE").scalar() or 0
    )

    available_beds = max(0, total_capacity - total_active_tenants)
    occupancy_pct = (
        round((total_active_tenants / total_capacity * 100), 2) if total_capacity > 0 else 0.0
    )

    pending_payments_count = (
        db.query(func.count(Payment.id)).filter(Payment.status == "PENDING").scalar() or 0
    )
    pending_payments_amount = (
        db.query(func.coalesce(func.sum(Payment.amount), Decimal("0.00")))
        .filter(Payment.status == "PENDING")
        .scalar()
        or Decimal("0.00")
    )

    open_complaints_count = (
        db.query(func.count(Complaint.id)).filter(Complaint.status.in_(["OPEN", "IN PROGRESS"])).scalar()
        or 0
    )

    pending_vacate_count = (
        db.query(func.count(VacateRequest.id)).filter(VacateRequest.status == "PENDING").scalar() or 0
    )

    return DashboardStats(
        total_rooms=total_rooms,
        total_capacity=total_capacity,
        total_active_tenants=total_active_tenants,
        available_beds=available_beds,
        occupancy_rate_percentage=occupancy_pct,
        pending_payments_count=pending_payments_count,
        pending_payments_amount=pending_payments_amount,
        open_complaints_count=open_complaints_count,
        pending_vacate_requests_count=pending_vacate_count,
    )


def get_financial_report(
    db: Session,
    year: Optional[int] = None,
) -> FinancialReport:
    """Generate financial summary and monthly breakdown of billed, collected, and outstanding rent."""
    query = db.query(
        RentCharge.billing_year,
        RentCharge.billing_month,
        func.coalesce(func.sum(RentCharge.amount_due), Decimal("0.00")).label("billed"),
        func.coalesce(func.sum(RentCharge.amount_paid), Decimal("0.00")).label("collected"),
    )

    if year is not None:
        query = query.filter(RentCharge.billing_year == year)

    rows = (
        query.group_by(RentCharge.billing_year, RentCharge.billing_month)
        .order_by(RentCharge.billing_year.desc(), RentCharge.billing_month.desc())
        .all()
    )

    breakdown: List[MonthlyFinancialRecord] = []
    tot_billed = Decimal("0.00")
    tot_collected = Decimal("0.00")

    for r in rows:
        billed = r.billed
        collected = r.collected
        outstanding = billed - collected
        tot_billed += billed
        tot_collected += collected

        breakdown.append(
            MonthlyFinancialRecord(
                year=r.billing_year,
                month=r.billing_month,
                total_billed=billed,
                total_collected=collected,
                total_outstanding=outstanding,
            )
        )

    summary = MonthlyFinancialRecord(
        year=year or 0,
        month=0,
        total_billed=tot_billed,
        total_collected=tot_collected,
        total_outstanding=tot_billed - tot_collected,
    )

    return FinancialReport(
        summary=summary,
        monthly_breakdown=breakdown,
    )
