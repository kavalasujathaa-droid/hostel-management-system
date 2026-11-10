package com.hostelmanagement.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.OwnerInitRequestDto
import com.hostelmanagement.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OwnerInitUiState(
    val hostelName: String = "",
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val address: String = "",
    val rentDueDay: String = "5",
    val bootstrapSecret: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class OwnerInitViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    }

    private val _uiState = MutableStateFlow(OwnerInitUiState())
    val uiState: StateFlow<OwnerInitUiState> = _uiState.asStateFlow()

    fun onHostelNameChange(v: String) = _uiState.update { it.copy(hostelName = v, errorMessage = null) }
    fun onFullNameChange(v: String) = _uiState.update { it.copy(fullName = v, errorMessage = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, errorMessage = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, errorMessage = null) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(phone = v, errorMessage = null) }
    fun onAddressChange(v: String) = _uiState.update { it.copy(address = v, errorMessage = null) }
    fun onRentDueDayChange(v: String) = _uiState.update { it.copy(rentDueDay = v, errorMessage = null) }
    fun onBootstrapSecretChange(v: String) = _uiState.update { it.copy(bootstrapSecret = v, errorMessage = null) }

    fun submit() {
        val s = _uiState.value
        if (s.hostelName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Hostel name is required") }
            return
        }
        if (s.fullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Owner full name is required") }
            return
        }
        if (s.email.isBlank() || !EMAIL_REGEX.matches(s.email.trim())) {
            _uiState.update { it.copy(errorMessage = "Valid email is required") }
            return
        }
        if (s.password.length < 8) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters") }
            return
        }
        val rentDay = s.rentDueDay.toIntOrNull()
        if (rentDay == null || rentDay !in 1..28) {
            _uiState.update { it.copy(errorMessage = "Rent due day must be between 1 and 28") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val request = OwnerInitRequestDto(
                hostel_name = s.hostelName.trim(),
                full_name = s.fullName.trim(),
                email = s.email.trim(),
                password = s.password.trim(),
                phone = s.phone.trim().ifBlank { null },
                address = s.address.trim().ifBlank { null },
                rent_due_day = rentDay,
                bootstrap_secret = s.bootstrapSecret.trim().ifBlank { null }
            )

            when (val result = authRepository.initOwner(request)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}
