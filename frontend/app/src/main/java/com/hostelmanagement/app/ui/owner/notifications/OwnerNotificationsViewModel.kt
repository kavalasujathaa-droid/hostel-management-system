package com.hostelmanagement.app.ui.owner.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.NotificationResponseDto
import com.hostelmanagement.app.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NotificationFilter(val label: String, val apiKey: String?) {
    ALL("All", null),
    PAYMENTS("Payments", "PAYMENT"),
    REMINDERS("Reminders", "REMINDER"),
    ALERTS("Alerts", "ALERT")
}

data class OwnerNotificationsUiState(
    val isLoading: Boolean = false,
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val notifications: List<NotificationResponseDto> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class OwnerNotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerNotificationsUiState())
    val uiState: StateFlow<OwnerNotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun selectFilter(filter: NotificationFilter) {
        if (_uiState.value.selectedFilter == filter) return
        _uiState.update { it.copy(selectedFilter = filter) }
        loadNotifications(filter)
    }

    fun loadNotifications(filter: NotificationFilter = _uiState.value.selectedFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = notificationRepository.listNotifications(filter.apiKey)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = result.data ?: emptyList()
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

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            when (notificationRepository.markAsRead(notificationId)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                if (it.id == notificationId) it.copy(is_read = true) else it
                            }
                        )
                    }
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            when (val result = notificationRepository.markAllAsRead()) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map { it.copy(is_read = true) },
                            successMessage = "All notifications marked as read"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
