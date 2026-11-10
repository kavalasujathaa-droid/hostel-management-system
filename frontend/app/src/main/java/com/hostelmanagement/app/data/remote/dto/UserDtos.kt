package com.hostelmanagement.app.data.remote.dto

data class UserResponseDto(
    val id: Long,
    val full_name: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val is_active: Boolean,
    val created_at: String
)

data class UserUpdateRequestDto(
    val full_name: String? = null,
    val phone: String? = null
)

data class PasswordChangeRequestDto(
    val old_password: String,
    val new_password: String
)

data class TenantCreateRequestDto(
    val full_name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val room_id: Long? = null,
    val joining_date: String? = null,
    val monthly_rent: Double? = null
)

data class TenantDetailResponseDto(
    val tenant: UserResponseDto,
    val active_stay: TenantStayResponseDto?,
    val stay_history: List<TenantStayResponseDto>?
)
