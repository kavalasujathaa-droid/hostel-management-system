package com.hostelmanagement.app.ui.owner.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.domain.repository.OwnerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageOwnersUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val owners: List<UserResponseDto> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ManageOwnersViewModel @Inject constructor(
    private val ownerRepository: OwnerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageOwnersUiState())
    val uiState: StateFlow<ManageOwnersUiState> = _uiState.asStateFlow()

    init {
        loadOwners()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadOwners()
    }

    fun loadOwners() {
        viewModelScope.launch {
            if (!_uiState.value.isRefreshing) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            when (val result = ownerRepository.listOwners()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            owners = result.data ?: emptyList(),
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = result.message ?: "Failed to load owner accounts"
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
