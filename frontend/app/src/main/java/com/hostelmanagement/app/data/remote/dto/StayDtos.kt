package com.hostelmanagement.app.data.remote.dto

data class TenantStayCreateDto(
    val tenant_user_id: Long,
    val room_id: Long,
    val joining_date: String,
    val monthly_rent: String
)

data class TenantStayResponseDto(
    val id: Long,
    val tenant_user_id: Long,
    val room_id: Long,
    val room_number: String? = null,
    val joining_date: String,
    val approved_vacating_date: String? = null,
    val monthly_rent: String,
    val status: String,
    val created_at: String
)
