package com.hostelmanagement.app.ui.owner.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.DashboardStatsDto
import com.hostelmanagement.app.data.remote.dto.PaymentResponseDto
import com.hostelmanagement.app.domain.repository.AuthRepository
import com.hostelmanagement.app.domain.repository.PaymentRepository
import com.hostelmanagement.app.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OwnerDashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val stats: DashboardStatsDto? = null,
    val pendingPayments: List<PaymentResponseDto> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class OwnerDashboardViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val paymentRepository: PaymentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerDashboardUiState())
    val uiState: StateFlow<OwnerDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            if (!_uiState.value.isRefreshing) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            // Fetch Stats
            val statsResult = reportRepository.getDashboardStats()
            // Fetch Pending Payments
            val paymentsResult = paymentRepository.listPayments(status = "PENDING")

            if (statsResult is Resource.Success && paymentsResult is Resource.Success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        stats = statsResult.data,
                        pendingPayments = paymentsResult.data,
                        errorMessage = null
                    )
                }
            } else {
                val errorMsg = (statsResult as? Resource.Error)?.message
                    ?: (paymentsResult as? Resource.Error)?.message
                    ?: "Failed to load dashboard overview"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }
}
