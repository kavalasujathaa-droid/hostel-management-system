from typing import Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

from app.core.dependencies import require_owner
from app.db.session import get_db
from app.models.models import User
from app.schemas.report import DashboardStats, FinancialReport
from app.services import report_service

router = APIRouter()


@router.get("/dashboard", response_model=DashboardStats, status_code=status.HTTP_200_OK)
def get_dashboard_summary(
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> DashboardStats:
    """Retrieve owner dashboard overview: room occupancy, active counts, pending approvals."""
    return report_service.get_dashboard_stats(db)


@router.get("/revenue", response_model=FinancialReport, status_code=status.HTTP_200_OK)
def get_revenue_report(
    year: Optional[int] = Query(None),
    current_user: User = Depends(require_owner),
    db: Session = Depends(get_db),
) -> FinancialReport:
    """Retrieve financial report breaking down billed, collected, and outstanding dues."""
    return report_service.get_financial_report(db, year=year)
