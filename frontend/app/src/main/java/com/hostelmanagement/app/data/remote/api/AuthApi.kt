package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.LoginRequestDto
import com.hostelmanagement.app.data.remote.dto.LogoutRequestDto
import com.hostelmanagement.app.data.remote.dto.MessageResponseDto
import com.hostelmanagement.app.data.remote.dto.OwnerInitRequestDto
import com.hostelmanagement.app.data.remote.dto.RefreshTokenRequestDto
import com.hostelmanagement.app.data.remote.dto.TokenResponseDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/init-owner")
    suspend fun initOwner(@Body request: OwnerInitRequestDto): TokenResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): TokenResponseDto

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequestDto): TokenResponseDto

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequestDto): MessageResponseDto

    @GET("auth/me")
    suspend fun getCurrentUser(): UserResponseDto
}
