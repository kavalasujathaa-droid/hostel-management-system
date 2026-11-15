package com.hostelmanagement.app

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.domain.repository.OwnerRepository
import com.hostelmanagement.app.ui.owner.management.AddOwnerViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddOwnerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val ownerRepository: OwnerRepository = mockk(relaxed = true)
    private lateinit var viewModel: AddOwnerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AddOwnerViewModel(ownerRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createOwner_validInputs_success() = runTest(testDispatcher) {
        val mockCreated = UserResponseDto(
            id = 5L,
            full_name = "New Co-Owner",
            email = "newowner@hostel.com",
            role = "OWNER",
            phone = "9876543210",
            is_active = true,
            created_at = "2026-11-15T12:00:00"
        )
        coEvery { ownerRepository.createOwner(any()) } returns Resource.Success(mockCreated)

        viewModel.onFullNameChange("New Co-Owner")
        viewModel.onEmailChange("newowner@hostel.com")
        viewModel.onPhoneChange("9876543210")
        viewModel.onPasswordChange("StrongPassword123")
        viewModel.onConfirmPasswordChange("StrongPassword123")

        viewModel.createOwner()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)
        assertNotNull(state.createdOwner)
        assertEquals("New Co-Owner", state.createdOwner?.full_name)
        assertNull(state.errorMessage)
    }

    @Test
    fun createOwner_fullNameTooShort_setsError() = runTest(testDispatcher) {
        viewModel.onFullNameChange("A")
        viewModel.onEmailChange("test@hostel.com")
        viewModel.onPasswordChange("StrongPassword123")
        viewModel.onConfirmPasswordChange("StrongPassword123")

        viewModel.createOwner()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("Full name must be at least 2 characters", state.fullNameError)
    }

    @Test
    fun createOwner_invalidEmail_setsError() = runTest(testDispatcher) {
        viewModel.onFullNameChange("Valid Name")
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("StrongPassword123")
        viewModel.onConfirmPasswordChange("StrongPassword123")

        viewModel.createOwner()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("Please enter a valid email address", state.emailError)
    }

    @Test
    fun createOwner_passwordTooShort_setsError() = runTest(testDispatcher) {
        viewModel.onFullNameChange("Valid Name")
        viewModel.onEmailChange("valid@hostel.com")
        viewModel.onPasswordChange("short") // < 8 chars
        viewModel.onConfirmPasswordChange("short")

        viewModel.createOwner()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("Password must be at least 8 characters", state.passwordError)
    }

    @Test
    fun createOwner_passwordMismatch_setsError() = runTest(testDispatcher) {
        viewModel.onFullNameChange("Valid Name")
        viewModel.onEmailChange("valid@hostel.com")
        viewModel.onPasswordChange("Password123")
        viewModel.onConfirmPasswordChange("DifferentPassword456")

        viewModel.createOwner()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("Passwords do not match", state.confirmPasswordError)
    }

    @Test
    fun createOwner_duplicateEmailConflict_setsErrorMessage() = runTest(testDispatcher) {
        coEvery { ownerRepository.createOwner(any()) } returns Resource.Error(
            message = "User with email 'duplicate@hostel.com' already exists",
            code = "CONFLICT"
        )

        viewModel.onFullNameChange("Valid Name")
        viewModel.onEmailChange("duplicate@hostel.com")
        viewModel.onPasswordChange("StrongPassword123")
        viewModel.onConfirmPasswordChange("StrongPassword123")

        viewModel.createOwner()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("User with email 'duplicate@hostel.com' already exists", state.errorMessage)
    }

    @Test
    fun togglePasswordVisibility_updatesVisibility() {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.togglePasswordVisibility()
        assertTrue(viewModel.uiState.value.isPasswordVisible)
        viewModel.togglePasswordVisibility()
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }
}
