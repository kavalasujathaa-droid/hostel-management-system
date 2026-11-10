package com.hostelmanagement.app.ui.owner.tenants

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.TenantDetailResponseDto
import com.hostelmanagement.app.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TenantDetailUiState(
    val tenantId: Long = 0,
    val isLoading: Boolean = false,
    val details: TenantDetailResponseDto? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TenantDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tenantRepository: TenantRepository
) : ViewModel() {

    private val tenantId: Long = savedStateHandle.get<Long>("tenantId") ?: 0

    private val _uiState = MutableStateFlow(TenantDetailUiState(tenantId = tenantId))
    val uiState: StateFlow<TenantDetailUiState> = _uiState.asStateFlow()

    init {
        loadTenantDetails()
    }

    fun loadTenantDetails() {
        if (tenantId == 0L) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = tenantRepository.getTenantDetails(tenantId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, details = result.data, errorMessage = null) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }
}
