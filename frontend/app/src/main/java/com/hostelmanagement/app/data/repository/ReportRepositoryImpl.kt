package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.ReportApi
import com.hostelmanagement.app.data.remote.dto.DashboardStatsDto
import com.hostelmanagement.app.data.remote.dto.FinancialReportDto
import com.hostelmanagement.app.domain.repository.ReportRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportApi: ReportApi,
    private val gson: Gson
) : ReportRepository {

    override suspend fun getDashboardStats(): Resource<DashboardStatsDto> {
        return safeApiCall(gson) { reportApi.getDashboardStats() }
    }

    override suspend fun getRevenueReport(year: Int?): Resource<FinancialReportDto> {
        return safeApiCall(gson) { reportApi.getRevenueReport(year = year) }
    }
}
