package com.hostelmanagement.app.domain.repository

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.datastore.UserSession
import com.hostelmanagement.app.data.remote.dto.LoginRequestDto
import com.hostelmanagement.app.data.remote.dto.MessageResponseDto
import com.hostelmanagement.app.data.remote.dto.OwnerInitRequestDto
import com.hostelmanagement.app.data.remote.dto.TokenResponseDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val sessionFlow: Flow<UserSession?>
    suspend fun login(request: LoginRequestDto): Resource<TokenResponseDto>
    suspend fun initOwner(request: OwnerInitRequestDto): Resource<TokenResponseDto>
    suspend fun logout(): Resource<MessageResponseDto>
    suspend fun getCurrentUser(): Resource<UserResponseDto>
    suspend fun clearSession()
}
