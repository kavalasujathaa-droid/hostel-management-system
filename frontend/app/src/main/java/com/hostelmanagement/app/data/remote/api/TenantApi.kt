package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.TenantCreateRequestDto
import com.hostelmanagement.app.data.remote.dto.TenantDetailResponseDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TenantApi {

    @POST("tenants")
    suspend fun createTenant(@Body request: TenantCreateRequestDto): UserResponseDto

    @GET("tenants")
    suspend fun listTenants(): List<UserResponseDto>

    @GET("tenants/{tenant_id}")
    suspend fun getTenantDetails(@Path("tenant_id") tenantId: Long): TenantDetailResponseDto
}
