from decimal import Decimal
from typing import Dict, List, Optional
from pydantic import BaseModel


class DashboardStats(BaseModel):
    total_rooms: int
    total_capacity: int
    total_active_tenants: int
    available_beds: int
    occupancy_rate_percentage: float
    pending_payments_count: int
    pending_payments_amount: Decimal
    open_complaints_count: int
    pending_vacate_requests_count: int


class MonthlyFinancialRecord(BaseModel):
    year: int
    month: int
    total_billed: Decimal
    total_collected: Decimal
    total_outstanding: Decimal


class FinancialReport(BaseModel):
    summary: MonthlyFinancialRecord
    monthly_breakdown: List[MonthlyFinancialRecord]
