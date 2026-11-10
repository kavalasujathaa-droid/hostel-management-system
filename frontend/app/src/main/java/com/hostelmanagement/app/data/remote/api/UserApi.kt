package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.MessageResponseDto
import com.hostelmanagement.app.data.remote.dto.PasswordChangeRequestDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.data.remote.dto.UserUpdateRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {

    @GET("users/me")
    suspend fun getProfile(): UserResponseDto

    @PUT("users/me")
    suspend fun updateProfile(@Body request: UserUpdateRequestDto): UserResponseDto

    @PUT("users/me/password")
    suspend fun changePassword(@Body request: PasswordChangeRequestDto): MessageResponseDto
}
