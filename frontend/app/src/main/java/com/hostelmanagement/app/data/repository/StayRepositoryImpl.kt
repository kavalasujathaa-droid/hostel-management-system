package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.StayApi
import com.hostelmanagement.app.data.remote.dto.TenantStayCreateDto
import com.hostelmanagement.app.data.remote.dto.TenantStayResponseDto
import com.hostelmanagement.app.domain.repository.StayRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StayRepositoryImpl @Inject constructor(
    private val stayApi: StayApi,
    private val gson: Gson
) : StayRepository {

    override suspend fun assignRoomStay(request: TenantStayCreateDto): Resource<TenantStayResponseDto> {
        return safeApiCall(gson) { stayApi.assignRoomStay(request) }
    }

    override suspend fun getMyActiveStay(): Resource<TenantStayResponseDto?> {
        return safeApiCall(gson) { stayApi.getMyActiveStay() }
    }

    override suspend fun getMyStayHistory(): Resource<List<TenantStayResponseDto>> {
        return safeApiCall(gson) { stayApi.getMyStayHistory() }
    }
}
