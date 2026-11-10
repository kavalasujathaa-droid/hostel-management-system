package com.hostelmanagement.app.ui.tenant.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.TenantStayResponseDto
import com.hostelmanagement.app.domain.repository.StayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TenantRoomInfoUiState(
    val isLoading: Boolean = false,
    val activeStay: TenantStayResponseDto? = null,
    val pastStays: List<TenantStayResponseDto> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class TenantRoomInfoViewModel @Inject constructor(
    private val stayRepository: StayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantRoomInfoUiState())
    val uiState: StateFlow<TenantRoomInfoUiState> = _uiState.asStateFlow()

    init {
        loadRoomInfo()
    }

    fun loadRoomInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val activeResult = stayRepository.getMyActiveStay()
            val historyResult = stayRepository.getMyStayHistory()

            val active = (activeResult as? Resource.Success)?.data
            val history = (historyResult as? Resource.Success)?.data ?: emptyList()
            val past = history.filter { it.status == "VACATED" }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    activeStay = active,
                    pastStays = past
                )
            }
        }
    }
}
