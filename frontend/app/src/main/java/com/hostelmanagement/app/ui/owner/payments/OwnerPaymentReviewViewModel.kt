package com.hostelmanagement.app.ui.owner.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.PaymentResponseDto
import com.hostelmanagement.app.data.remote.dto.PaymentReviewRequestDto
import com.hostelmanagement.app.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OwnerPaymentReviewUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val payments: List<PaymentResponseDto> = emptyList(),
    val selectedTab: Int = 0, // 0: Pending, 1: All
    val isRejectDialogOpen: Boolean = false,
    val targetPaymentId: Long = 0,
    val isProcessingAction: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val pendingPayments: List<PaymentResponseDto>
        get() = payments.filter { it.status.equals("PENDING", ignoreCase = true) }

    val reviewedPayments: List<PaymentResponseDto>
        get() = payments.filter { !it.status.equals("PENDING", ignoreCase = true) }

    val historyPayments: List<PaymentResponseDto>
        get() = reviewedPayments
}

@HiltViewModel
class OwnerPaymentReviewViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerPaymentReviewUiState())
    val uiState: StateFlow<OwnerPaymentReviewUiState> = _uiState.asStateFlow()

    init {
        loadPayments()
    }

    fun loadPayments() {
        viewModelScope.launch {
            if (!_uiState.value.isRefreshing) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            when (val result = paymentRepository.listPayments()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            payments = result.data,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadPayments()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun approvePayment(paymentId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingAction = true) }
            when (val result = paymentRepository.approvePayment(paymentId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessingAction = false,
                            successMessage = "Payment of ${result.data.amount} approved and marked PAID"
                        )
                    }
                    loadPayments()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessingAction = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun openRejectDialog(paymentId: Long) {
        _uiState.update { it.copy(isRejectDialogOpen = true, targetPaymentId = paymentId) }
    }

    fun closeRejectDialog() {
        _uiState.update { it.copy(isRejectDialogOpen = false, targetPaymentId = 0) }
    }

    fun confirmRejectPayment(reason: String) {
        val paymentId = _uiState.value.targetPaymentId
        if (paymentId == 0L) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingAction = true) }
            val req = PaymentReviewRequestDto(rejection_reason = reason.trim().ifBlank { "Invalid transaction" })
            when (val result = paymentRepository.rejectPayment(paymentId, req)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessingAction = false,
                            isRejectDialogOpen = false,
                            targetPaymentId = 0,
                            successMessage = "Payment rejected successfully"
                        )
                    }
                    loadPayments()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessingAction = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }
}
