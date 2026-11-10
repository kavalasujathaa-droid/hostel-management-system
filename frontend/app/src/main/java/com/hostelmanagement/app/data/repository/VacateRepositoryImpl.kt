package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.VacateApi
import com.hostelmanagement.app.data.remote.dto.VacateRequestCreateDto
import com.hostelmanagement.app.data.remote.dto.VacateRequestResponseDto
import com.hostelmanagement.app.data.remote.dto.VacateRequestReviewDto
import com.hostelmanagement.app.domain.repository.VacateRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VacateRepositoryImpl @Inject constructor(
    private val vacateApi: VacateApi,
    private val gson: Gson
) : VacateRepository {

    override suspend fun submitVacateRequest(request: VacateRequestCreateDto): Resource<VacateRequestResponseDto> {
        return safeApiCall(gson) { vacateApi.submitVacateRequest(request) }
    }

    override suspend fun listVacateRequests(status: String?): Resource<List<VacateRequestResponseDto>> {
        return safeApiCall(gson) { vacateApi.listVacateRequests(status = status) }
    }

    override suspend fun reviewVacateRequest(
        requestId: Long,
        request: VacateRequestReviewDto
    ): Resource<VacateRequestResponseDto> {
        return safeApiCall(gson) { vacateApi.reviewVacateRequest(requestId, request) }
    }
}
