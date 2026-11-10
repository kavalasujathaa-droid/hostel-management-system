package com.hostelmanagement.app.data.remote.dto

data class PaymentQRResponseDto(
    val id: Long,
    val image_path: String,
    val is_active: Boolean,
    val created_at: String
)
