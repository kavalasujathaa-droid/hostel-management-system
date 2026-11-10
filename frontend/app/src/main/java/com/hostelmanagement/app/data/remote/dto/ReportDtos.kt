package com.hostelmanagement.app.data.remote.dto

data class DashboardStatsDto(
    val total_rooms: Int,
    val total_capacity: Int,
    val total_active_tenants: Int,
    val available_beds: Int,
    val occupancy_rate_percentage: Double,
    val pending_payments_count: Int,
    val pending_payments_amount: String,
    val open_complaints_count: Int,
    val pending_vacate_requests_count: Int
)

data class MonthlyFinancialRecordDto(
    val year: Int,
    val month: Int,
    val total_billed: String,
    val total_collected: String,
    val total_outstanding: String
)

data class FinancialReportDto(
    val summary: MonthlyFinancialRecordDto,
    val monthly_breakdown: List<MonthlyFinancialRecordDto>
)
