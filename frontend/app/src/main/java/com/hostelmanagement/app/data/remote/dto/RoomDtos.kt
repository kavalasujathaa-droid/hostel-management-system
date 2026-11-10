package com.hostelmanagement.app.data.remote.dto

data class OccupantInfoDto(
    val tenant_id: Long,
    val full_name: String,
    val email: String,
    val joining_date: String,
    val stay_id: Long
)

data class RoomResponseDto(
    val id: Long,
    val room_number: String,
    val floor: String? = null,
    val capacity: Int,
    val is_active: Boolean,
    val created_at: String,
    val active_occupants: Int = 0,
    val available_beds: Int = 0,
    val occupants: List<OccupantInfoDto>? = null
)

data class RoomCreateDto(
    val room_number: String,
    val floor: String? = null,
    val capacity: Int
)

data class RoomUpdateDto(
    val floor: String? = null,
    val capacity: Int? = null,
    val is_active: Boolean? = null
)
