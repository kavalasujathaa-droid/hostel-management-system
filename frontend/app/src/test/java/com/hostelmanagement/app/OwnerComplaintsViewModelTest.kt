package com.hostelmanagement.app

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.ComplaintResponseDto
import com.hostelmanagement.app.data.remote.dto.ComplaintUpdateDto
import com.hostelmanagement.app.domain.repository.ComplaintRepository
import com.hostelmanagement.app.ui.owner.complaints.OwnerComplaintsViewModel
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OwnerComplaintsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val complaintRepository: ComplaintRepository = mockk(relaxed = true)
    private lateinit var viewModel: OwnerComplaintsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { complaintRepository.listComplaints(any()) } returns Resource.Success(emptyList())
        coEvery { complaintRepository.listComplaints(null) } returns Resource.Success(emptyList())
        viewModel = OwnerComplaintsViewModel(complaintRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun setStatusFilter_updatesStateAndQueriesRepository() = runTest(testDispatcher) {
        viewModel.setStatusFilter("OPEN")
        advanceUntilIdle()

        assertEquals("OPEN", viewModel.uiState.value.selectedStatusFilter)
        coVerify { complaintRepository.listComplaints("OPEN") }
    }

    @Test
    fun updateStatus_resolvedStatus_allowedAndDispatched() = runTest(testDispatcher) {
        val mockResponse = ComplaintResponseDto(
            id = 5L,
            tenant_stay_id = 1L,
            title = "Tap fixed",
            category = "Plumbing",
            description = "Washbasin tap fixed",
            status = "RESOLVED",
            created_at = "2026-11-10T10:00:00",
            updated_at = "2026-11-10T11:00:00"
        )
        coEvery { complaintRepository.updateComplaintStatus(5L, match { it.status == "RESOLVED" }) } returns Resource.Success(mockResponse)

        viewModel.updateStatus(5L, "RESOLVED")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isUpdating)
        assertEquals("Complaint status updated to RESOLVED", state.successMessage)
        coVerify(exactly = 1) { complaintRepository.updateComplaintStatus(5L, ComplaintUpdateDto("RESOLVED")) }
    }

    @Test
    fun updateStatus_inProgressStatus_allowedAndDispatched() = runTest(testDispatcher) {
        val mockResponse = ComplaintResponseDto(
            id = 5L,
            tenant_stay_id = 1L,
            title = "Tap inspection",
            category = "Plumbing",
            description = "Plumber assigned",
            status = "IN PROGRESS",
            created_at = "2026-11-10T10:00:00",
            updated_at = "2026-11-10T11:00:00"
        )
        coEvery { complaintRepository.updateComplaintStatus(5L, match { it.status == "IN PROGRESS" }) } returns Resource.Success(mockResponse)

        viewModel.updateStatus(5L, "IN PROGRESS")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isUpdating)
        assertEquals("Complaint status updated to IN PROGRESS", state.successMessage)
        coVerify(exactly = 1) { complaintRepository.updateComplaintStatus(5L, ComplaintUpdateDto("IN PROGRESS")) }
    }

    @Test
    fun updateStatus_invalidStatus_ignored() = runTest(testDispatcher) {
        viewModel.updateStatus(5L, "CLOSED")
        advanceUntilIdle()

        coVerify(exactly = 0) { complaintRepository.updateComplaintStatus(any(), any()) }
    }
}
