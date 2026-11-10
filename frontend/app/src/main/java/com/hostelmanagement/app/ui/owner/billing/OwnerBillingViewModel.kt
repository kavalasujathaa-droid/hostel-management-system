package com.hostelmanagement.app.ui.owner.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.BillingGenerateRequestDto
import com.hostelmanagement.app.data.remote.dto.RentChargeResponseDto
import com.hostelmanagement.app.domain.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class OwnerBillingUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val charges: List<RentChargeResponseDto> = emptyList(),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val isGeneratingBills: Boolean = false,
    val isGenerateDialogOpen: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class OwnerBillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerBillingUiState())
    val uiState: StateFlow<OwnerBillingUiState> = _uiState.asStateFlow()

    init {
        loadCharges()
    }

    fun loadCharges(year: Int = _uiState.value.selectedYear, month: Int = _uiState.value.selectedMonth) {
        viewModelScope.launch {
            if (!_uiState.value.isRefreshing) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            when (val result = billingRepository.listCharges(year = year, month = month)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            charges = result.data,
                            selectedYear = year,
                            selectedMonth = month,
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
        loadCharges()
    }

    fun openGenerateDialog() = _uiState.update { it.copy(isGenerateDialogOpen = true) }
    fun closeGenerateDialog() = _uiState.update { it.copy(isGenerateDialogOpen = false) }

    fun generateBilling(year: Int, month: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingBills = true) }
            when (val result = billingRepository.generateBills(BillingGenerateRequestDto(year = year, month = month))) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isGeneratingBills = false,
                            isGenerateDialogOpen = false,
                            successMessage = "Generated ${result.data.size} rent charge records successfully"
                        )
                    }
                    loadCharges(year = year, month = month)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isGeneratingBills = false,
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
