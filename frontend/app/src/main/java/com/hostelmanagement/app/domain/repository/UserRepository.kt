package com.hostelmanagement.app.domain.repository

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.MessageResponseDto
import com.hostelmanagement.app.data.remote.dto.PasswordChangeRequestDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.data.remote.dto.UserUpdateRequestDto

interface UserRepository {
    suspend fun getProfile(): Resource<UserResponseDto>
    suspend fun updateProfile(request: UserUpdateRequestDto): Resource<UserResponseDto>
    suspend fun changePassword(request: PasswordChangeRequestDto): Resource<MessageResponseDto>
}
