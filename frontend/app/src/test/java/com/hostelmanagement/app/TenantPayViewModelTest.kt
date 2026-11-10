package com.hostelmanagement.app

import androidx.lifecycle.SavedStateHandle
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.PaymentQRResponseDto
import com.hostelmanagement.app.data.remote.dto.PaymentResponseDto
import com.hostelmanagement.app.data.remote.dto.PaymentSubmissionRequestDto
import com.hostelmanagement.app.data.remote.dto.RentChargeResponseDto
import com.hostelmanagement.app.domain.repository.BillingRepository
import com.hostelmanagement.app.domain.repository.PaymentQrRepository
import com.hostelmanagement.app.domain.repository.PaymentRepository
import com.hostelmanagement.app.ui.tenant.payments.TenantPayViewModel
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
class TenantPayViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val paymentRepository: PaymentRepository = mockk(relaxed = true)
    private val paymentQrRepository: PaymentQrRepository = mockk(relaxed = true)
    private val billingRepository: BillingRepository = mockk(relaxed = true)
    private val savedStateHandle = SavedStateHandle(mapOf("rentChargeId" to 101L))

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadPaymentData_success_loadsChargeAndActiveQr() = runTest(testDispatcher) {
        val mockQr = PaymentQRResponseDto(
            id = 1L,
            image_path = "/uploads/qr/active_qr.png",
            is_active = true,
            created_at = "2026-11-01T00:00:00"
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
                amount_paid = "0.00",
                balance = "6000.00",
                due_date = "2026-11-05",
                status = "PENDING",
                created_at = "2026-11-01T00:00:00"
            )
        )

        coEvery { paymentQrRepository.getActiveQr() } returns Resource.Success(mockQr)
        coEvery { billingRepository.listCharges() } returns Resource.Success(mockCharges)

        val viewModel = TenantPayViewModel(savedStateHandle, paymentRepository, paymentQrRepository, billingRepository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.activeQr)
        assertEquals(101L, viewModel.uiState.value.rentCharge?.id)
        assertEquals("6000.00", viewModel.uiState.value.amountInput)
    }

    @Test
    fun submitPayment_invalidAmount_setsError() = runTest(testDispatcher) {
        val viewModel = TenantPayViewModel(savedStateHandle, paymentRepository, paymentQrRepository, billingRepository)
        advanceUntilIdle()

        viewModel.onAmountChanged("0")
        viewModel.onUtrChanged("UTR123456789")
        viewModel.submitPayment()

        assertEquals("Please enter a valid positive payment amount", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun submitPayment_shortUtr_setsError() = runTest(testDispatcher) {
        val viewModel = TenantPayViewModel(savedStateHandle, paymentRepository, paymentQrRepository, billingRepository)
        advanceUntilIdle()

        viewModel.onAmountChanged("5000.00")
        viewModel.onUtrChanged("12")
        viewModel.submitPayment()

        assertEquals("UTR reference number must be at least 4 characters", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun submitPayment_valid_callsRepositoryAndSetsSuccess() = runTest(testDispatcher) {
        val mockPaymentResponse = PaymentResponseDto(
            id = 301L,
            rent_charge_id = 101L,
            submitted_by = 5L,
            amount = "6000.00",
            utr = "UTR11223344",
            status = "PENDING",
            rejection_reason = null,
            submitted_at = "2026-11-10T12:00:00",
            reviewed_at = null
        )

        coEvery {
            paymentRepository.submitPayment(
                PaymentSubmissionRequestDto(rent_charge_id = 101L, amount = "6000.00", utr = "UTR11223344"),
                any()
            )
        } returns Resource.Success(mockPaymentResponse)

        val viewModel = TenantPayViewModel(savedStateHandle, paymentRepository, paymentQrRepository, billingRepository)
        advanceUntilIdle()

        viewModel.onAmountChanged("6000.00")
        viewModel.onUtrChanged("UTR11223344")
        viewModel.submitPayment()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSubmitting)
        assertTrue(viewModel.uiState.value.paymentSubmitted)
        assertNull(viewModel.uiState.value.errorMessage)

        coVerify(exactly = 1) {
            paymentRepository.submitPayment(
                PaymentSubmissionRequestDto(rent_charge_id = 101L, amount = "6000.00", utr = "UTR11223344"),
                any()
            )
        }
    }
}
