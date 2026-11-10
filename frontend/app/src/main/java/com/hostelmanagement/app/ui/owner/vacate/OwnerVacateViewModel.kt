package com.hostelmanagement.app.ui.owner.vacate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.VacateRequestResponseDto
import com.hostelmanagement.app.data.remote.dto.VacateRequestReviewDto
import com.hostelmanagement.app.domain.repository.VacateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OwnerVacateUiState(
    val isLoading: Boolean = false,
    val isReviewing: Boolean = false,
    val requests: List<VacateRequestResponseDto> = emptyList(),
    val selectedStatusFilter: String? = "PENDING", // default to pending notices
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class OwnerVacateViewModel @Inject constructor(
    private val vacateRepository: VacateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerVacateUiState())
    val uiState: StateFlow<OwnerVacateUiState> = _uiState.asStateFlow()

    init {
        loadVacateRequests()
    }

    fun setStatusFilter(status: String?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
        loadVacateRequests(status)
    }

    fun loadVacateRequests(status: String? = _uiState.value.selectedStatusFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = vacateRepository.listVacateRequests(status)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            requests = result.data ?: emptyList()
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

    fun approveRequest(requestId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewing = true, errorMessage = null) }
            val request = VacateRequestReviewDto(action = "APPROVE", rejection_reason = null)
            when (val result = vacateRepository.reviewVacateRequest(requestId, request)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isReviewing = false,
                            successMessage = "Vacate request approved. Stay end date updated."
                        )
                    }
                    loadVacateRequests()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isReviewing = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun rejectRequest(requestId: Long, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReviewing = true, errorMessage = null) }
            val request = VacateRequestReviewDto(
                action = "REJECT",
                rejection_reason = reason.ifBlank { null }
            )
            when (val result = vacateRepository.reviewVacateRequest(requestId, request)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isReviewing = false,
                            successMessage = "Vacate request rejected."
                        )
                    }
                    loadVacateRequests()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isReviewing = false,
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
