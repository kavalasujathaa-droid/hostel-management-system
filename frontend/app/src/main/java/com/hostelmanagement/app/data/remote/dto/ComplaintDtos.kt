package com.hostelmanagement.app.data.remote.dto

data class ComplaintCreateDto(
    val title: String,
    val category: String = "Other",
    val tenant_stay_id: Long? = null,
    val description: String
)

data class ComplaintUpdateDto(
    val status: String // "OPEN", "IN PROGRESS", "RESOLVED"
)

data class ComplaintResponseDto(
    val id: Long,
    val tenant_stay_id: Long,
    val tenant_name: String? = null,
    val room_number: String? = null,
    val description: String,
    val status: String,
    val created_at: String,
    val updated_at: String,
    val title: String = "",
    val category: String = "Other",
    val attachment_path: String? = null
)
