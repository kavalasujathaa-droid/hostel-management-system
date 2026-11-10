package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.MessageResponseDto
import com.hostelmanagement.app.data.remote.dto.NotificationResponseDto
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {

    @GET("notifications")
    suspend fun listNotifications(
        @Query("category") category: String? = null
    ): List<NotificationResponseDto>

    @PUT("notifications/{notification_id}/read")
    suspend fun markAsRead(@Path("notification_id") notificationId: Long): MessageResponseDto

    @PUT("notifications/read-all")
    suspend fun markAllAsRead(): MessageResponseDto
}
