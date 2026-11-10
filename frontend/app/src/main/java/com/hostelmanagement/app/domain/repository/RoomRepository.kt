package com.hostelmanagement.app.domain.repository

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.MessageResponseDto
import com.hostelmanagement.app.data.remote.dto.RoomCreateDto
import com.hostelmanagement.app.data.remote.dto.RoomResponseDto
import com.hostelmanagement.app.data.remote.dto.RoomUpdateDto

interface RoomRepository {
    suspend fun listRooms(floor: String? = null, isActive: Boolean? = null): Resource<List<RoomResponseDto>>
    suspend fun createRoom(request: RoomCreateDto): Resource<RoomResponseDto>
    suspend fun getRoom(roomId: Long): Resource<RoomResponseDto>
    suspend fun updateRoom(roomId: Long, request: RoomUpdateDto): Resource<RoomResponseDto>
    suspend fun deleteRoom(roomId: Long): Resource<MessageResponseDto>
}
