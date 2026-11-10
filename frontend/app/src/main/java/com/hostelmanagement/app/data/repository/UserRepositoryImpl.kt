package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.UserApi
import com.hostelmanagement.app.data.remote.dto.MessageResponseDto
import com.hostelmanagement.app.data.remote.dto.PasswordChangeRequestDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.data.remote.dto.UserUpdateRequestDto
import com.hostelmanagement.app.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val gson: Gson
) : UserRepository {

    override suspend fun getProfile(): Resource<UserResponseDto> {
        return safeApiCall(gson) { userApi.getProfile() }
    }

    override suspend fun updateProfile(request: UserUpdateRequestDto): Resource<UserResponseDto> {
        return safeApiCall(gson) { userApi.updateProfile(request) }
    }

    override suspend fun changePassword(request: PasswordChangeRequestDto): Resource<MessageResponseDto> {
        return safeApiCall(gson) { userApi.changePassword(request) }
    }
}
