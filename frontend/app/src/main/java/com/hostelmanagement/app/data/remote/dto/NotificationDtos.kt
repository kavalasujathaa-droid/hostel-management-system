package com.hostelmanagement.app.data.remote.dto

data class NotificationResponseDto(
    val id: Long,
    val user_id: Long,
    val title: String,
    val message: String,
    val notification_type: String,
    val category: String = "GENERAL",
    val is_read: Boolean,
    val created_at: String
)

data class DeviceTokenRegisterDto(
    val fcm_token: String,
    val platform: String = "ANDROID"
)
