package com.hostelmanagement.app.domain.repository

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.DashboardStatsDto
import com.hostelmanagement.app.data.remote.dto.FinancialReportDto

interface ReportRepository {
    suspend fun getDashboardStats(): Resource<DashboardStatsDto>
    suspend fun getRevenueReport(year: Int? = null): Resource<FinancialReportDto>
}
