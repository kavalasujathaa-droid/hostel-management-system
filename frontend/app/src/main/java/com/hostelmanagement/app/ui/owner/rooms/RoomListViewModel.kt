package com.hostelmanagement.app.ui.owner.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.RoomCreateDto
import com.hostelmanagement.app.data.remote.dto.RoomResponseDto
import com.hostelmanagement.app.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val rooms: List<RoomResponseDto> = emptyList(),
    val selectedFloor: String? = null,
    val isAddRoomDialogOpen: Boolean = false,
    val isSubmittingRoom: Boolean = false,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

@HiltViewModel
class RoomListViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomListUiState())
    val uiState: StateFlow<RoomListUiState> = _uiState.asStateFlow()

    init {
        loadRooms()
    }

    fun loadRooms(floor: String? = _uiState.value.selectedFloor) {
        viewModelScope.launch {
            if (!_uiState.value.isRefreshing) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            when (val result = roomRepository.listRooms(floor = floor)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            rooms = result.data,
                            selectedFloor = floor,
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
        loadRooms()
    }

    fun setFloorFilter(floor: String?) {
        loadRooms(floor = floor)
    }

    fun openAddRoomDialog() = _uiState.update { it.copy(isAddRoomDialogOpen = true) }
    fun closeAddRoomDialog() = _uiState.update { it.copy(isAddRoomDialogOpen = false) }

    fun createRoom(roomNumber: String, floor: String?, capacity: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingRoom = true) }
            val request = RoomCreateDto(
                room_number = roomNumber.trim(),
                floor = floor?.trim()?.ifBlank { null },
                capacity = capacity
            )
            when (val result = roomRepository.createRoom(request)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingRoom = false,
                            isAddRoomDialogOpen = false,
                            actionSuccessMessage = "Room ${result.data.room_number} created successfully"
                        )
                    }
                    loadRooms()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingRoom = false,
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
