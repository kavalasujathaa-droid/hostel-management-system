package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.OwnerCreateRequestDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OwnerApi {

    @POST("owners")
    suspend fun createOwner(@Body request: OwnerCreateRequestDto): UserResponseDto

    @GET("owners")
    suspend fun listOwners(): List<UserResponseDto>
}
