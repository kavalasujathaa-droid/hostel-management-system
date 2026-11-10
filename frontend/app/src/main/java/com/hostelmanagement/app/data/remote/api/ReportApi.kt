package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.DashboardStatsDto
import com.hostelmanagement.app.data.remote.dto.FinancialReportDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ReportApi {

    @GET("reports/dashboard")
    suspend fun getDashboardStats(): DashboardStatsDto

    @GET("reports/revenue")
    suspend fun getRevenueReport(@Query("year") year: Int? = null): FinancialReportDto
}
