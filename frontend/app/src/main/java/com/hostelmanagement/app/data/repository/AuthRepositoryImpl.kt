package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.datastore.SessionManager
import com.hostelmanagement.app.core.datastore.UserSession
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.AuthApi
import com.hostelmanagement.app.data.remote.dto.LoginRequestDto
import com.hostelmanagement.app.data.remote.dto.LogoutRequestDto
import com.hostelmanagement.app.data.remote.dto.MessageResponseDto
import com.hostelmanagement.app.data.remote.dto.OwnerInitRequestDto
import com.hostelmanagement.app.data.remote.dto.TokenResponseDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager,
    private val gson: Gson
) : AuthRepository {

    override val sessionFlow: Flow<UserSession?> = sessionManager.sessionFlow

    override suspend fun login(request: LoginRequestDto): Resource<TokenResponseDto> {
        val result = safeApiCall(gson) { authApi.login(request) }
        if (result is Resource.Success) {
            val tokenData = result.data
            sessionManager.saveSession(
                UserSession(
                    accessToken = tokenData.access_token,
                    refreshToken = tokenData.refresh_token,
                    userId = tokenData.user.id,
                    fullName = tokenData.user.full_name,
                    email = tokenData.user.email,
                    role = tokenData.user.role,
                    isActive = tokenData.user.is_active
                )
            )
        }
        return result
    }

    override suspend fun initOwner(request: OwnerInitRequestDto): Resource<TokenResponseDto> {
        val result = safeApiCall(gson) { authApi.initOwner(request) }
        if (result is Resource.Success) {
            val tokenData = result.data
            sessionManager.saveSession(
                UserSession(
                    accessToken = tokenData.access_token,
                    refreshToken = tokenData.refresh_token,
                    userId = tokenData.user.id,
                    fullName = tokenData.user.full_name,
                    email = tokenData.user.email,
                    role = tokenData.user.role,
                    isActive = tokenData.user.is_active
                )
            )
        }
        return result
    }

    override suspend fun logout(): Resource<MessageResponseDto> {
        val refreshToken = sessionManager.getRefreshToken()
        val result = safeApiCall(gson) {
            authApi.logout(LogoutRequestDto(refresh_token = refreshToken))
        }
        sessionManager.clearSession()
        return result
    }

    override suspend fun getCurrentUser(): Resource<UserResponseDto> {
        return safeApiCall(gson) { authApi.getCurrentUser() }
    }

    override suspend fun clearSession() {
        sessionManager.clearSession()
    }
}
