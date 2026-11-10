package com.hostelmanagement.app.ui.tenant.payments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.PaymentQRResponseDto
import com.hostelmanagement.app.data.remote.dto.PaymentSubmissionRequestDto
import com.hostelmanagement.app.data.remote.dto.RentChargeResponseDto
import com.hostelmanagement.app.domain.repository.BillingRepository
import com.hostelmanagement.app.domain.repository.PaymentQrRepository
import com.hostelmanagement.app.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class TenantPayUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val rentChargeId: Long = 0L,
    val rentCharge: RentChargeResponseDto? = null,
    val activeQr: PaymentQRResponseDto? = null,
    val amountInput: String = "",
    val utrInput: String = "",
    val errorMessage: String? = null,
    val paymentSubmitted: Boolean = false
)

@HiltViewModel
class TenantPayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paymentRepository: PaymentRepository,
    private val paymentQrRepository: PaymentQrRepository,
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val rentChargeId: Long = checkNotNull(savedStateHandle["rentChargeId"])

    private val _uiState = MutableStateFlow(TenantPayUiState(rentChargeId = rentChargeId))
    val uiState: StateFlow<TenantPayUiState> = _uiState.asStateFlow()

    init {
        loadPaymentData()
    }

    fun loadPaymentData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val qrResult = paymentQrRepository.getActiveQr()
            val billingResult = billingRepository.listCharges()

            val activeQr = (qrResult as? Resource.Success)?.data
            val rentCharges = (billingResult as? Resource.Success)?.data ?: emptyList()
            val matchedCharge = rentCharges.firstOrNull { it.id == rentChargeId }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    activeQr = activeQr,
                    rentCharge = matchedCharge,
                    amountInput = matchedCharge?.balance ?: ""
                )
            }
        }
    }

    fun onAmountChanged(newAmount: String) {
        _uiState.update { it.copy(amountInput = newAmount) }
    }

    fun onUtrChanged(newUtr: String) {
        _uiState.update { it.copy(utrInput = newUtr) }
    }

    fun submitPayment() {
        val amount = _uiState.value.amountInput.trim()
        val utr = _uiState.value.utrInput.trim()

        if (amount.isBlank() || amount.toBigDecimalOrNull() == null || amount.toBigDecimal() <= BigDecimal.ZERO) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid positive payment amount") }
            return
        }

        if (utr.length < 4) {
            _uiState.update { it.copy(errorMessage = "UTR reference number must be at least 4 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val request = PaymentSubmissionRequestDto(
                rent_charge_id = rentChargeId,
                amount = amount,
                utr = utr
            )

            when (val result = paymentRepository.submitPayment(request)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            paymentSubmitted = true
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
