package com.hostelmanagement.app.data.remote.dto

data class MessageResponseDto(
    val success: Boolean,
    val message: String
)

data class HealthStatusDto(
    val status: String,
    val app: String,
    val environment: String
)

data class HealthReadyDto(
    val status: String,
    val database: String
)

data class ErrorResponseDto(
    val success: Boolean,
    val error: ErrorDetailDto
)

data class ErrorDetailDto(
    val code: String,
    val message: String,
    val details: Any? = null
)
