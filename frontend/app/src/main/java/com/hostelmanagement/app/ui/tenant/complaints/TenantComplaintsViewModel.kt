package com.hostelmanagement.app.ui.tenant.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.ComplaintCreateDto
import com.hostelmanagement.app.data.remote.dto.ComplaintResponseDto
import com.hostelmanagement.app.domain.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TenantComplaintsUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val complaints: List<ComplaintResponseDto> = emptyList(),
    val selectedStatusFilter: String? = null, // null for all, "OPEN", "IN PROGRESS", "RESOLVED"
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class TenantComplaintsViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantComplaintsUiState())
    val uiState: StateFlow<TenantComplaintsUiState> = _uiState.asStateFlow()

    init {
        loadComplaints()
    }

    fun setStatusFilter(status: String?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
        loadComplaints(status)
    }

    fun loadComplaints(status: String? = _uiState.value.selectedStatusFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = complaintRepository.listComplaints(status)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            complaints = result.data ?: emptyList()
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

    fun submitComplaint(description: String) {
        val trimmed = description.trim()
        if (trimmed.length < 5) {
            _uiState.update { it.copy(errorMessage = "Description must be at least 5 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val request = ComplaintCreateDto(
                title = "General Complaint",
                category = "Other",
                tenant_stay_id = null,
                description = trimmed
            )
            when (val result = complaintRepository.createComplaint(request)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Complaint submitted successfully"
                        )
                    }
                    loadComplaints()
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
