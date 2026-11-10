package com.hostelmanagement.app.data.remote.dto

data class NotificationResponseDto(
    val id: Long,
    val user_id: Long,
    val title: String,
    val message: String,
    val notification_type: String,
    val is_read: Boolean,
    val created_at: String
)
