package com.hostelmanagement.app

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.TokenResponseDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.domain.repository.AuthRepository
import com.hostelmanagement.app.ui.auth.LoginViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_invalidEmail_setsErrorMessage() {
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("password123")
        viewModel.login()

        assertEquals("Please enter a valid email address", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun login_emptyPassword_setsErrorMessage() {
        viewModel.onEmailChange("owner@example.com")
        viewModel.onPasswordChange("")
        viewModel.login()

        assertEquals("Password cannot be empty", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun login_success_setsUserRole() = runTest(testDispatcher) {
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
            access_token = "access_token_123",
            refresh_token = "refresh_token_123",
            token_type = "bearer",
            expires_in = 1800,
            user = mockUser
        )
        coEvery { authRepository.login(any()) } returns Resource.Success(mockToken)

        viewModel.onEmailChange("owner@example.com")
        viewModel.onPasswordChange("correct_password")
        viewModel.login()

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("OWNER", viewModel.uiState.value.userRole)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun login_failure_setsErrorMessage() = runTest(testDispatcher) {
        coEvery { authRepository.login(any()) } returns Resource.Error("Invalid credentials")

        viewModel.onEmailChange("tenant@example.com")
        viewModel.onPasswordChange("wrong_password")
        viewModel.login()

        advanceUntilIdle()

        assertEquals("Invalid credentials", viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.userRole)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
