package com.hostelmanagement.app

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.datastore.SessionManager
import com.hostelmanagement.app.data.remote.api.AuthApi
import com.hostelmanagement.app.data.remote.dto.LoginRequestDto
import com.hostelmanagement.app.data.remote.dto.MessageResponseDto
import com.hostelmanagement.app.data.remote.dto.TokenResponseDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.data.repository.AuthRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private val authApi: AuthApi = mockk(relaxed = true)
    private val sessionManager: SessionManager = mockk(relaxed = true)
    private val gson = Gson()
    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setUp() {
        authRepository = AuthRepositoryImpl(authApi, sessionManager, gson)
    }

    @Test
    fun login_success_savesSessionAndReturnsSuccess() = runTest {
        val mockUser = UserResponseDto(
            id = 1L,
            full_name = "Hostel Owner",
            email = "owner@example.com",
            role = "OWNER",
            phone = null,
            is_active = true,
            created_at = "2026-01-01T00:00:00"
        )
        val mockToken = TokenResponseDto(
            access_token = "access_token_abc",
            refresh_token = "refresh_token_xyz",
            token_type = "bearer",
            expires_in = 1800,
            user = mockUser
        )

        coEvery { authApi.login(any()) } returns Response.success(mockToken)

        val result = authRepository.login(LoginRequestDto("owner@example.com", "password"))

        assertTrue(result is Resource.Success)
        assertEquals("OWNER", (result as Resource.Success).data.user.role)
        coVerify(exactly = 1) {
            sessionManager.saveSession(
                match {
                    it.accessToken == "access_token_abc" &&
                    it.refreshToken == "refresh_token_xyz" &&
                    it.role == "OWNER"
                }
            )
        }
    }

    @Test
    fun logout_clearsSessionAlways() = runTest {
        coEvery { sessionManager.getRefreshToken() } returns "refresh_token_xyz"
        coEvery { authApi.logout(any()) } returns Response.success(MessageResponseDto(message = "Logged out"))

        val result = authRepository.logout()

        assertTrue(result is Resource.Success)
        coVerify(exactly = 1) { sessionManager.clearSession() }
    }
}
