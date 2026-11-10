package com.hostelmanagement.app.ui.owner.complaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.ComplaintResponseDto
import com.hostelmanagement.app.data.remote.dto.ComplaintUpdateDto
import com.hostelmanagement.app.domain.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OwnerComplaintsUiState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val complaints: List<ComplaintResponseDto> = emptyList(),
    val selectedStatusFilter: String? = null, // null for all, "OPEN", "IN PROGRESS"
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class OwnerComplaintsViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerComplaintsUiState())
    val uiState: StateFlow<OwnerComplaintsUiState> = _uiState.asStateFlow()

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

    fun updateStatus(complaintId: Long, newStatus: String) {
        if (newStatus != "OPEN" && newStatus != "IN PROGRESS") return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, errorMessage = null) }
            val request = ComplaintUpdateDto(status = newStatus)
            when (val result = complaintRepository.updateComplaintStatus(complaintId, request)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            successMessage = "Complaint status updated to $newStatus"
                        )
                    }
                    loadComplaints()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
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
