package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.NotificationApi
import com.hostelmanagement.app.data.remote.dto.MessageResponseDto
import com.hostelmanagement.app.data.remote.dto.NotificationResponseDto
import com.hostelmanagement.app.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi,
    private val gson: Gson
) : NotificationRepository {

    override suspend fun listNotifications(category: String?): Resource<List<NotificationResponseDto>> {
        return safeApiCall(gson) { notificationApi.listNotifications(category) }
    }

    override suspend fun markAsRead(notificationId: Long): Resource<MessageResponseDto> {
        return safeApiCall(gson) { notificationApi.markAsRead(notificationId) }
    }

    override suspend fun markAllAsRead(): Resource<MessageResponseDto> {
        return safeApiCall(gson) { notificationApi.markAllAsRead() }
    }
}
