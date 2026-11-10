package com.hostelmanagement.app.domain.repository

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.TenantCreateRequestDto
import com.hostelmanagement.app.data.remote.dto.TenantDetailResponseDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto

interface TenantRepository {
    suspend fun listTenants(): Resource<List<UserResponseDto>>
    suspend fun createTenant(request: TenantCreateRequestDto): Resource<UserResponseDto>
    suspend fun getTenantDetails(tenantId: Long): Resource<TenantDetailResponseDto>
}
