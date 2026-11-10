package com.hostelmanagement.app

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.DashboardStatsDto
import com.hostelmanagement.app.data.remote.dto.PaymentResponseDto
import com.hostelmanagement.app.domain.repository.AuthRepository
import com.hostelmanagement.app.domain.repository.PaymentRepository
import com.hostelmanagement.app.domain.repository.ReportRepository
import com.hostelmanagement.app.ui.owner.dashboard.OwnerDashboardViewModel
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OwnerDashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val reportRepository: ReportRepository = mockk(relaxed = true)
    private val paymentRepository: PaymentRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadDashboardData_success_updatesStatsAndPayments() = runTest(testDispatcher) {
        val mockStats = DashboardStatsDto(
            total_rooms = 10,
            total_capacity = 20,
            total_active_tenants = 15,
            available_beds = 5,
            occupancy_rate_percentage = 75.0,
            pending_payments_count = 2,
            pending_payments_amount = "10000.00",
            open_complaints_count = 1,
            pending_vacate_requests_count = 0
        )
        val mockPayments = listOf(
            PaymentResponseDto(
                id = 1L,
                rent_charge_id = 101L,
                submitted_by = 2L,
                amount = "5000.00",
                utr = "UTR99887766",
                status = "PENDING",
                rejection_reason = null,
                submitted_at = "2026-11-10T10:00:00",
                reviewed_at = null
            )
        )

        coEvery { reportRepository.getDashboardStats() } returns Resource.Success(mockStats)
        coEvery { paymentRepository.listPayments(status = "PENDING") } returns Resource.Success(mockPayments)

        val viewModel = OwnerDashboardViewModel(reportRepository, paymentRepository, authRepository)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.stats)
        assertEquals(10, viewModel.uiState.value.stats?.total_rooms)
        assertEquals(1, viewModel.uiState.value.pendingPayments.size)
    }

    @Test
    fun loadDashboardData_error_setsErrorMessage() = runTest(testDispatcher) {
        coEvery { reportRepository.getDashboardStats() } returns Resource.Error("Network error occurred")
        coEvery { paymentRepository.listPayments(any()) } returns Resource.Success(emptyList())

        val viewModel = OwnerDashboardViewModel(reportRepository, paymentRepository, authRepository)
        advanceUntilIdle()

        assertEquals("Network error occurred", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
