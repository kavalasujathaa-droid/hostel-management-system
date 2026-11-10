package com.hostelmanagement.app.ui.owner.tenants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.RoomResponseDto
import com.hostelmanagement.app.data.remote.dto.TenantCreateRequestDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.domain.repository.RoomRepository
import com.hostelmanagement.app.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TenantListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val tenants: List<UserResponseDto> = emptyList(),
    val searchQuery: String = "",
    val availableRooms: List<RoomResponseDto> = emptyList(),
    val isRegisterDialogOpen: Boolean = false,
    val isSubmittingTenant: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
) {
    val filteredTenants: List<UserResponseDto>
        get() {
            if (searchQuery.isBlank()) return tenants
            val query = searchQuery.trim().lowercase()
            return tenants.filter {
                it.full_name.lowercase().contains(query) ||
                it.email.lowercase().contains(query) ||
                (it.phone?.contains(query) == true)
            }
        }
}

@HiltViewModel
class TenantListViewModel @Inject constructor(
    private val tenantRepository: TenantRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantListUiState())
    val uiState: StateFlow<TenantListUiState> = _uiState.asStateFlow()

    init {
        loadTenants()
    }

    fun loadTenants() {
        viewModelScope.launch {
            if (!_uiState.value.isRefreshing) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            when (val result = tenantRepository.listTenants()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            tenants = result.data,
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
        loadTenants()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openRegisterDialog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRegisterDialogOpen = true) }
            val roomsResult = roomRepository.listRooms(isActive = true)
            if (roomsResult is Resource.Success) {
                _uiState.update { it.copy(availableRooms = roomsResult.data.filter { r -> r.available_beds > 0 }) }
            }
        }
    }

    fun closeRegisterDialog() = _uiState.update { it.copy(isRegisterDialogOpen = false) }

    fun registerTenant(
        fullName: String,
        email: String,
        password: String,
        phone: String?,
        roomId: Long?,
        joiningDate: String?,
        monthlyRent: Double?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingTenant = true) }
            val req = TenantCreateRequestDto(
                full_name = fullName.trim(),
                email = email.trim(),
                password = password.trim(),
                phone = phone?.trim()?.ifBlank { null },
                room_id = roomId,
                joining_date = joiningDate,
                monthly_rent = monthlyRent
            )
            when (val result = tenantRepository.createTenant(req)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingTenant = false,
                            isRegisterDialogOpen = false,
                            actionSuccessMessage = "Tenant ${result.data.full_name} registered successfully"
                        )
                    }
                    loadTenants()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingTenant = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearActionMessage() = _uiState.update { it.copy(actionSuccessMessage = null) }
}
