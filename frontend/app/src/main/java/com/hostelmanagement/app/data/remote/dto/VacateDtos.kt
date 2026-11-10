package com.hostelmanagement.app.data.remote.dto

data class VacateRequestCreateDto(
    val tenant_stay_id: Long? = null,
    val requested_vacating_date: String
)

data class VacateRequestReviewDto(
    val action: String, // "APPROVE" or "REJECT"
    val rejection_reason: String? = null
)

data class VacateRequestResponseDto(
    val id: Long,
    val tenant_stay_id: Long,
    val tenant_name: String? = null,
    val room_number: String? = null,
    val requested_vacating_date: String,
    val status: String,
    val reviewed_by: Long? = null,
    val rejection_reason: String? = null,
    val requested_at: String,
    val reviewed_at: String? = null
)
