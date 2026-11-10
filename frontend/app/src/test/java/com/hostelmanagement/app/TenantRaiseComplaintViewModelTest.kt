package com.hostelmanagement.app

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.ComplaintResponseDto
import com.hostelmanagement.app.domain.repository.ComplaintRepository
import com.hostelmanagement.app.ui.tenant.complaints.TenantRaiseComplaintViewModel
import io.mockk.coEvery
import io.mockk.coVerify
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
class TenantRaiseComplaintViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val complaintRepository: ComplaintRepository = mockk(relaxed = true)
    private lateinit var viewModel: TenantRaiseComplaintViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TenantRaiseComplaintViewModel(complaintRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onFieldChanges_updatesUiStateProperly() {
        viewModel.onTitleChanged("Leaking Tap")
        viewModel.onCategoryChanged("Plumbing")
        viewModel.onDescriptionChanged("Bathroom washbasin tap has a constant leak")

        val state = viewModel.uiState.value
        assertEquals("Leaking Tap", state.title)
        assertEquals("Plumbing", state.category)
        assertEquals("Bathroom washbasin tap has a constant leak", state.description)
        assertNull(state.errorMessage)
    }

    @Test
    fun submitComplaint_titleTooShort_setsErrorMessage() = runTest(testDispatcher) {
        viewModel.onTitleChanged("ab") // < 3 chars
        viewModel.onDescriptionChanged("Valid long description")
        viewModel.submitComplaint()

        val state = viewModel.uiState.value
        assertEquals("Title must be at least 3 characters", state.errorMessage)
        assertFalse(state.isSuccess)
        coVerify(exactly = 0) { complaintRepository.createComplaint(any(), any()) }
    }

    @Test
    fun submitComplaint_descriptionTooShort_setsErrorMessage() = runTest(testDispatcher) {
        viewModel.onTitleChanged("Valid Title")
        viewModel.onDescriptionChanged("leak") // < 5 chars
        viewModel.submitComplaint()

        val state = viewModel.uiState.value
        assertEquals("Description must be at least 5 characters", state.errorMessage)
        assertFalse(state.isSuccess)
        coVerify(exactly = 0) { complaintRepository.createComplaint(any(), any()) }
    }

    @Test
    fun submitComplaint_success_callsRepositoryAndSetsSuccess() = runTest(testDispatcher) {
        val mockResponse = ComplaintResponseDto(
            id = 10L,
            tenant_stay_id = 1L,
            title = "Fan not working",
            category = "Electrical",
            description = "Ceiling fan is completely unresponsive",
            status = "OPEN",
            created_at = "2026-11-10T10:00:00",
            updated_at = "2026-11-10T10:00:00"
        )
        coEvery { complaintRepository.createComplaint(any(), any()) } returns Resource.Success(mockResponse)
        coEvery { complaintRepository.createComplaint(any(), null) } returns Resource.Success(mockResponse)

        viewModel.onTitleChanged("Fan not working")
        viewModel.onCategoryChanged("Electrical")
        viewModel.onDescriptionChanged("Ceiling fan is completely unresponsive")

        viewModel.submitComplaint()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertTrue(state.isSuccess)
        assertNull(state.errorMessage)
        coVerify(exactly = 1) { complaintRepository.createComplaint(any(), null) }
    }

    @Test
    fun submitComplaint_error_setsErrorMessage() = runTest(testDispatcher) {
        coEvery { complaintRepository.createComplaint(any(), any()) } returns Resource.Error("Server error occurred")
        coEvery { complaintRepository.createComplaint(any(), null) } returns Resource.Error("Server error occurred")

        viewModel.onTitleChanged("Fan not working")
        viewModel.onCategoryChanged("Electrical")
        viewModel.onDescriptionChanged("Ceiling fan is completely unresponsive")

        viewModel.submitComplaint()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertFalse(state.isSuccess)
        assertEquals("Server error occurred", state.errorMessage)
    }
}
