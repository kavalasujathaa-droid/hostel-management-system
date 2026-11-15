package com.hostelmanagement.app

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.domain.repository.OwnerRepository
import com.hostelmanagement.app.ui.owner.management.ManageOwnersViewModel
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
class ManageOwnersViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val ownerRepository: OwnerRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadOwners_success_rendersOwnerList() = runTest(testDispatcher) {
        val mockOwners = listOf(
            UserResponseDto(
                id = 1L,
                full_name = "Primary Owner",
                email = "primary@hostel.com",
                role = "OWNER",
                phone = "9876543210",
                is_active = true,
                created_at = "2026-01-01T00:00:00"
            ),
            UserResponseDto(
                id = 2L,
                full_name = "Co-Owner Bob",
                email = "bob@hostel.com",
                role = "OWNER",
                phone = "9876543211",
                is_active = true,
                created_at = "2026-02-01T00:00:00"
            )
        )
        coEvery { ownerRepository.listOwners() } returns Resource.Success(mockOwners)

        val viewModel = ManageOwnersViewModel(ownerRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(2, state.owners.size)
        assertEquals("Primary Owner", state.owners[0].full_name)
        assertEquals("Co-Owner Bob", state.owners[1].full_name)
    }

    @Test
    fun loadOwners_error_setsErrorMessage() = runTest(testDispatcher) {
        coEvery { ownerRepository.listOwners() } returns Resource.Error("Network error")

        val viewModel = ManageOwnersViewModel(ownerRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.errorMessage)
        assertEquals(0, state.owners.size)
    }

    @Test
    fun loadOwners_tenantAccessRestriction_setsForbiddenError() = runTest(testDispatcher) {
        coEvery { ownerRepository.listOwners() } returns Resource.Error(
            message = "Access forbidden: Owner role required",
            code = "FORBIDDEN"
        )

        val viewModel = ManageOwnersViewModel(ownerRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Access forbidden: Owner role required", state.errorMessage)
    }
}
