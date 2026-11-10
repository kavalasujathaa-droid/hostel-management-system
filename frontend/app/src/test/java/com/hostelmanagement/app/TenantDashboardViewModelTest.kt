package com.hostelmanagement.app

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.ComplaintResponseDto
import com.hostelmanagement.app.data.remote.dto.PaymentResponseDto
import com.hostelmanagement.app.data.remote.dto.RentChargeResponseDto
import com.hostelmanagement.app.data.remote.dto.TenantStayResponseDto
import com.hostelmanagement.app.domain.repository.BillingRepository
import com.hostelmanagement.app.domain.repository.ComplaintRepository
import com.hostelmanagement.app.domain.repository.PaymentRepository
import com.hostelmanagement.app.domain.repository.StayRepository
import com.hostelmanagement.app.ui.tenant.dashboard.TenantDashboardViewModel
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
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class TenantDashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val stayRepository: StayRepository = mockk(relaxed = true)
    private val billingRepository: BillingRepository = mockk(relaxed = true)
    private val paymentRepository: PaymentRepository = mockk(relaxed = true)
    private val complaintRepository: ComplaintRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadDashboard_success_aggregatesStayAndPendingDues() = runTest(testDispatcher) {
        val mockStay = TenantStayResponseDto(
            id = 1L,
            tenant_user_id = 5L,
            room_id = 10L,
            room_number = "101",
            joining_date = "2026-01-01",
            approved_vacating_date = null,
            monthly_rent = "6000.00",
            status = "ACTIVE",
            created_at = "2026-01-01T00:00:00"
        )
        val mockCharges = listOf(
            RentChargeResponseDto(
                id = 101L,
                tenant_stay_id = 1L,
                room_number = "101",
                tenant_name = "Tenant User",
                billing_year = 2026,
                billing_month = 11,
                period_start = "2026-11-01",
                period_end = "2026-11-30",
                monthly_rent_snapshot = "6000.00",
                days_in_month = 30,
                billable_days = 30,
                amount_due = "6000.00",
                amount_paid = "2000.00",
                balance = "4000.00",
                due_date = "2026-11-05",
                status = "PARTIAL",
                created_at = "2026-11-01T00:00:00"
            )
        )
        val mockPayments = listOf(
            PaymentResponseDto(
                id = 201L,
                rent_charge_id = 101L,
                submitted_by = 5L,
                amount = "2000.00",
                utr = "UTR11223344",
                status = "APPROVED",
                rejection_reason = null,
                submitted_at = "2026-11-02T10:00:00",
                reviewed_at = "2026-11-02T11:00:00"
            )
        )
        val mockComplaints = listOf(
            ComplaintResponseDto(
                id = 301L,
                tenant_stay_id = 1L,
                tenant_name = "Tenant User",
                room_number = "101",
                description = "Geyser leaking",
                status = "OPEN",
                created_at = "2026-11-05T09:00:00",
                updated_at = "2026-11-05T09:00:00"
            )
        )

        coEvery { stayRepository.getMyActiveStay() } returns Resource.Success(mockStay)
        coEvery { billingRepository.listCharges() } returns Resource.Success(mockCharges)
        coEvery { paymentRepository.listPayments() } returns Resource.Success(mockPayments)
        coEvery { complaintRepository.listComplaints() } returns Resource.Success(mockComplaints)

        val viewModel = TenantDashboardViewModel(stayRepository, billingRepository, paymentRepository, complaintRepository)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.activeStay)
        assertEquals("101", viewModel.uiState.value.activeStay?.room_number)
        assertEquals(BigDecimal("4000.00"), viewModel.uiState.value.totalOutstanding)
        assertEquals(1, viewModel.uiState.value.recentPayments.size)
        assertEquals(1, viewModel.uiState.value.activeComplaints.size)
    }
}
