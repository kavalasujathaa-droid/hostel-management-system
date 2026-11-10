package com.hostelmanagement.app.domain.repository

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.TenantStayCreateDto
import com.hostelmanagement.app.data.remote.dto.TenantStayResponseDto

interface StayRepository {
    suspend fun assignRoomStay(request: TenantStayCreateDto): Resource<TenantStayResponseDto>
    suspend fun getMyActiveStay(): Resource<TenantStayResponseDto?>
    suspend fun getMyStayHistory(): Resource<List<TenantStayResponseDto>>
}
