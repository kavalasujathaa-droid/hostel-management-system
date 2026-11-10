package com.hostelmanagement.app.data.remote.dto

data class ComplaintCreateDto(
    val tenant_stay_id: Long? = null,
    val description: String
)

data class ComplaintUpdateDto(
    val status: String // "OPEN" or "IN PROGRESS"
)

data class ComplaintResponseDto(
    val id: Long,
    val tenant_stay_id: Long,
    val tenant_name: String? = null,
    val room_number: String? = null,
    val description: String,
    val status: String,
    val created_at: String,
    val updated_at: String
)
