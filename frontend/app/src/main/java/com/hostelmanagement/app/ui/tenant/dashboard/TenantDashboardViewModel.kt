package com.hostelmanagement.app.ui.tenant.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.ComplaintResponseDto
import com.hostelmanagement.app.data.remote.dto.PaymentResponseDto
import com.hostelmanagement.app.data.remote.dto.RentChargeResponseDto
import com.hostelmanagement.app.data.remote.dto.TenantStayResponseDto
import com.hostelmanagement.app.domain.repository.BillingRepository
import com.hostelmanagement.app.domain.repository.ComplaintRepository
import com.hostelmanagement.app.domain.repository.PaymentRepository
import com.hostelmanagement.app.domain.repository.StayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class TenantDashboardUiState(
    val isLoading: Boolean = false,
    val activeStay: TenantStayResponseDto? = null,
    val unpaidRentCharge: RentChargeResponseDto? = null,
    val totalOutstanding: BigDecimal = BigDecimal.ZERO,
    val recentPayments: List<PaymentResponseDto> = emptyList(),
    val activeComplaints: List<ComplaintResponseDto> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class TenantDashboardViewModel @Inject constructor(
    private val stayRepository: StayRepository,
    private val billingRepository: BillingRepository,
    private val paymentRepository: PaymentRepository,
    private val complaintRepository: ComplaintRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantDashboardUiState())
    val uiState: StateFlow<TenantDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val stayResult = stayRepository.getMyActiveStay()
            val billingResult = billingRepository.listCharges()
            val paymentResult = paymentRepository.listPayments()
            val complaintResult = complaintRepository.listComplaints()

            val activeStay = (stayResult as? Resource.Success)?.data
            val rentCharges = (billingResult as? Resource.Success)?.data ?: emptyList()
            val payments = (paymentResult as? Resource.Success)?.data ?: emptyList()
            val complaints = (complaintResult as? Resource.Success)?.data ?: emptyList()

            // Find oldest unpaid or partially paid rent charge
            val unpaid = rentCharges.firstOrNull { it.status != "PAID" }
            val totalOutstanding = rentCharges
                .filter { it.status != "PAID" }
                .sumOf { BigDecimal(it.balance) }

            val errorMsg = listOfNotNull(
                (stayResult as? Resource.Error)?.message,
                (billingResult as? Resource.Error)?.message,
                (paymentResult as? Resource.Error)?.message,
                (complaintResult as? Resource.Error)?.message
            ).firstOrNull()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    activeStay = activeStay,
                    unpaidRentCharge = unpaid,
                    totalOutstanding = totalOutstanding,
                    recentPayments = payments.take(3),
                    activeComplaints = complaints.filter { c -> c.status == "OPEN" || c.status == "IN PROGRESS" },
                    errorMessage = if (activeStay == null && rentCharges.isEmpty() && payments.isEmpty()) errorMsg else null
                )
            }
        }
    }
}
