package com.hostelmanagement.app.ui.owner.rooms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.RoomResponseDto
import com.hostelmanagement.app.data.remote.dto.RoomUpdateDto
import com.hostelmanagement.app.data.remote.dto.TenantStayCreateDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.domain.repository.RoomRepository
import com.hostelmanagement.app.domain.repository.StayRepository
import com.hostelmanagement.app.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomDetailUiState(
    val roomId: Long = 0,
    val isLoading: Boolean = false,
    val room: RoomResponseDto? = null,
    val availableTenants: List<UserResponseDto> = emptyList(),
    val isAssignBedDialogOpen: Boolean = false,
    val isAssigningBed: Boolean = false,
    val isDeletingRoom: Boolean = false,
    val isRoomDeleted: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class RoomDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val roomRepository: RoomRepository,
    private val stayRepository: StayRepository,
    private val tenantRepository: TenantRepository
) : ViewModel() {

    private val roomId: Long = savedStateHandle.get<Long>("roomId") ?: 0

    private val _uiState = MutableStateFlow(RoomDetailUiState(roomId = roomId))
    val uiState: StateFlow<RoomDetailUiState> = _uiState.asStateFlow()

    init {
        loadRoomDetails()
    }

    fun loadRoomDetails() {
        if (roomId == 0L) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = roomRepository.getRoom(roomId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, room = result.data, errorMessage = null) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun openAssignBedDialog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAssignBedDialogOpen = true) }
            val tenantsResult = tenantRepository.listTenants()
            if (tenantsResult is Resource.Success) {
                _uiState.update { it.copy(availableTenants = tenantsResult.data) }
            }
        }
    }

    fun closeAssignBedDialog() = _uiState.update { it.copy(isAssignBedDialogOpen = false) }

    fun assignBed(tenantId: Long, joiningDate: String, monthlyRent: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAssigningBed = true) }
            val req = TenantStayCreateDto(
                tenant_user_id = tenantId,
                room_id = roomId,
                joining_date = joiningDate,
                monthly_rent = monthlyRent
            )
            when (val result = stayRepository.assignRoomStay(req)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isAssigningBed = false,
                            isAssignBedDialogOpen = false,
                            successMessage = "Tenant assigned successfully to Room ${result.data.room_number ?: roomId}"
                        )
                    }
                    loadRoomDetails()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isAssigningBed = false, errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteRoom() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingRoom = true) }
            when (val result = roomRepository.deleteRoom(roomId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isDeletingRoom = false, isRoomDeleted = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isDeletingRoom = false, errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }
}
