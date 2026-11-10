package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.TenantStayCreateDto
import com.hostelmanagement.app.data.remote.dto.TenantStayResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface StayApi {

    @POST("stays")
    suspend fun assignRoomStay(@Body request: TenantStayCreateDto): TenantStayResponseDto

    @GET("stays/active")
    suspend fun getMyActiveStay(): TenantStayResponseDto?

    @GET("stays/history")
    suspend fun getMyStayHistory(): List<TenantStayResponseDto>
}
