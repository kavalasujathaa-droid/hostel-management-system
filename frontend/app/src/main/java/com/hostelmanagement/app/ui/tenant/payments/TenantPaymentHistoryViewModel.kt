package com.hostelmanagement.app.ui.tenant.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.PaymentResponseDto
import com.hostelmanagement.app.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TenantPaymentHistoryUiState(
    val isLoading: Boolean = false,
    val payments: List<PaymentResponseDto> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class TenantPaymentHistoryViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantPaymentHistoryUiState())
    val uiState: StateFlow<TenantPaymentHistoryUiState> = _uiState.asStateFlow()

    init {
        loadPayments()
    }

    fun loadPayments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = paymentRepository.listPayments()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            payments = result.data ?: emptyList()
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }
}
