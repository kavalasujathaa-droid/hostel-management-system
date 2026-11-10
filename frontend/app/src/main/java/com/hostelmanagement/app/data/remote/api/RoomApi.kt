package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.MessageResponseDto
import com.hostelmanagement.app.data.remote.dto.RoomCreateDto
import com.hostelmanagement.app.data.remote.dto.RoomResponseDto
import com.hostelmanagement.app.data.remote.dto.RoomUpdateDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RoomApi {

    @GET("rooms")
    suspend fun listRooms(
        @Query("floor") floor: String? = null,
        @Query("is_active") isActive: Boolean? = null
    ): List<RoomResponseDto>

    @POST("rooms")
    suspend fun createRoom(@Body request: RoomCreateDto): RoomResponseDto

    @GET("rooms/{room_id}")
    suspend fun getRoom(@Path("room_id") roomId: Long): RoomResponseDto

    @PUT("rooms/{room_id}")
    suspend fun updateRoom(
        @Path("room_id") roomId: Long,
        @Body request: RoomUpdateDto
    ): RoomResponseDto

    @DELETE("rooms/{room_id}")
    suspend fun deleteRoom(@Path("room_id") roomId: Long): MessageResponseDto
}
