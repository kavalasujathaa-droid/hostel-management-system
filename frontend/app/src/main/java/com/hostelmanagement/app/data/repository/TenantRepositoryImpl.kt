package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.TenantApi
import com.hostelmanagement.app.data.remote.dto.TenantCreateRequestDto
import com.hostelmanagement.app.data.remote.dto.TenantDetailResponseDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.domain.repository.TenantRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TenantRepositoryImpl @Inject constructor(
    private val tenantApi: TenantApi,
    private val gson: Gson
) : TenantRepository {

    override suspend fun listTenants(): Resource<List<UserResponseDto>> {
        return safeApiCall(gson) { tenantApi.listTenants() }
    }

    override suspend fun createTenant(request: TenantCreateRequestDto): Resource<UserResponseDto> {
        return safeApiCall(gson) { tenantApi.createTenant(request) }
    }

    override suspend fun getTenantDetails(tenantId: Long): Resource<TenantDetailResponseDto> {
        return safeApiCall(gson) { tenantApi.getTenantDetails(tenantId) }
    }
}
