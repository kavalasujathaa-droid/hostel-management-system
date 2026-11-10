package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.RoomApi
import com.hostelmanagement.app.data.remote.dto.MessageResponseDto
import com.hostelmanagement.app.data.remote.dto.RoomCreateDto
import com.hostelmanagement.app.data.remote.dto.RoomResponseDto
import com.hostelmanagement.app.data.remote.dto.RoomUpdateDto
import com.hostelmanagement.app.domain.repository.RoomRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepositoryImpl @Inject constructor(
    private val roomApi: RoomApi,
    private val gson: Gson
) : RoomRepository {

    override suspend fun listRooms(floor: String?, isActive: Boolean?): Resource<List<RoomResponseDto>> {
        return safeApiCall(gson) { roomApi.listRooms(floor = floor, isActive = isActive) }
    }

    override suspend fun createRoom(request: RoomCreateDto): Resource<RoomResponseDto> {
        return safeApiCall(gson) { roomApi.createRoom(request) }
    }

    override suspend fun getRoom(roomId: Long): Resource<RoomResponseDto> {
        return safeApiCall(gson) { roomApi.getRoom(roomId) }
    }

    override suspend fun updateRoom(roomId: Long, request: RoomUpdateDto): Resource<RoomResponseDto> {
        return safeApiCall(gson) { roomApi.updateRoom(roomId, request) }
    }

    override suspend fun deleteRoom(roomId: Long): Resource<MessageResponseDto> {
        return safeApiCall(gson) { roomApi.deleteRoom(roomId) }
    }
}
