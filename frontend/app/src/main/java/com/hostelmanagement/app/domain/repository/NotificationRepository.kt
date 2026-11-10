package com.hostelmanagement.app.domain.repository

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.MessageResponseDto
import com.hostelmanagement.app.data.remote.dto.NotificationResponseDto

interface NotificationRepository {
    suspend fun listNotifications(category: String? = null): Resource<List<NotificationResponseDto>>
    suspend fun markAsRead(notificationId: Long): Resource<MessageResponseDto>
    suspend fun markAllAsRead(): Resource<MessageResponseDto>
    suspend fun registerDeviceToken(fcmToken: String): Resource<MessageResponseDto>
    suspend fun unregisterDeviceToken(fcmToken: String): Resource<MessageResponseDto>
}
