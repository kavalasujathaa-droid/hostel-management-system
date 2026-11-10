package com.hostelmanagement.app.ui.tenant.vacate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.VacateRequestCreateDto
import com.hostelmanagement.app.data.remote.dto.VacateRequestResponseDto
import com.hostelmanagement.app.domain.repository.VacateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TenantVacateUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val requests: List<VacateRequestResponseDto> = emptyList(),
    val activeRequest: VacateRequestResponseDto? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class TenantVacateViewModel @Inject constructor(
    private val vacateRepository: VacateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantVacateUiState())
    val uiState: StateFlow<TenantVacateUiState> = _uiState.asStateFlow()

    init {
        loadVacateRequests()
    }

    fun loadVacateRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = vacateRepository.listVacateRequests()) {
                is Resource.Success -> {
                    val list = result.data ?: emptyList()
                    val active = list.firstOrNull { it.status == "PENDING" || it.status == "APPROVED" }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            requests = list,
                            activeRequest = active
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

    fun submitVacateNotice(vacatingDate: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val request = VacateRequestCreateDto(
                tenant_stay_id = null,
                requested_vacating_date = vacatingDate
            )
            when (val result = vacateRepository.submitVacateRequest(request)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Move-out notice submitted for owner review"
                        )
                    }
                    loadVacateRequests()
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

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
