package com.hostelmanagement.app.domain.repository

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.VacateRequestCreateDto
import com.hostelmanagement.app.data.remote.dto.VacateRequestResponseDto
import com.hostelmanagement.app.data.remote.dto.VacateRequestReviewDto

interface VacateRepository {
    suspend fun submitVacateRequest(request: VacateRequestCreateDto): Resource<VacateRequestResponseDto>
    suspend fun listVacateRequests(status: String? = null): Resource<List<VacateRequestResponseDto>>
    suspend fun reviewVacateRequest(requestId: Long, request: VacateRequestReviewDto): Resource<VacateRequestResponseDto>
}
