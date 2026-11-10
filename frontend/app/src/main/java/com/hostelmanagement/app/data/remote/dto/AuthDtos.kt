package com.hostelmanagement.app.data.remote.dto

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class OwnerInitRequestDto(
    val full_name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val hostel_name: String,
    val address: String? = null,
    val rent_due_day: Int = 5,
    val bootstrap_secret: String? = null
)

data class RefreshTokenRequestDto(
    val refresh_token: String
)

data class LogoutRequestDto(
    val refresh_token: String? = null
)

data class TokenResponseDto(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val expires_in: Int,
    val user: UserResponseDto
)
