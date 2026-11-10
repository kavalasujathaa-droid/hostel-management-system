package com.hostelmanagement.app.ui.tenant.payments

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.PaymentQRResponseDto
import com.hostelmanagement.app.data.remote.dto.PaymentSubmissionRequestDto
import com.hostelmanagement.app.data.remote.dto.RentChargeResponseDto
import com.hostelmanagement.app.domain.repository.BillingRepository
import com.hostelmanagement.app.domain.repository.PaymentQrRepository
import com.hostelmanagement.app.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigDecimal
import javax.inject.Inject

data class TenantPayUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val rentChargeId: Long = 0L,
    val rentCharge: RentChargeResponseDto? = null,
    val activeQr: PaymentQRResponseDto? = null,
    val amountInput: String = "",
    val utrInput: String = "",
    val selectedImageUri: Uri? = null,
    val selectedImageBytes: ByteArray? = null,
    val selectedImageName: String? = null,
    val selectedImageMimeType: String? = null,
    val errorMessage: String? = null,
    val paymentSubmitted: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TenantPayUiState

        if (isLoading != other.isLoading) return false
        if (isSubmitting != other.isSubmitting) return false
        if (rentChargeId != other.rentChargeId) return false
        if (rentCharge != other.rentCharge) return false
        if (activeQr != other.activeQr) return false
        if (amountInput != other.amountInput) return false
        if (utrInput != other.utrInput) return false
        if (selectedImageUri != other.selectedImageUri) return false
        if (selectedImageBytes != null) {
            if (other.selectedImageBytes == null) return false
            if (!selectedImageBytes.contentEquals(other.selectedImageBytes)) return false
        } else if (other.selectedImageBytes != null) return false
        if (selectedImageName != other.selectedImageName) return false
        if (selectedImageMimeType != other.selectedImageMimeType) return false
        if (errorMessage != other.errorMessage) return false
        if (paymentSubmitted != other.paymentSubmitted) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isLoading.hashCode()
        result = 31 * result + isSubmitting.hashCode()
        result = 31 * result + rentChargeId.hashCode()
        result = 31 * result + (rentCharge?.hashCode() ?: 0)
        result = 31 * result + (activeQr?.hashCode() ?: 0)
        result = 31 * result + amountInput.hashCode()
        result = 31 * result + utrInput.hashCode()
        result = 31 * result + (selectedImageUri?.hashCode() ?: 0)
        result = 31 * result + (selectedImageBytes?.contentHashCode() ?: 0)
        result = 31 * result + (selectedImageName?.hashCode() ?: 0)
        result = 31 * result + (selectedImageMimeType?.hashCode() ?: 0)
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + paymentSubmitted.hashCode()
        return result
    }
}

@HiltViewModel
class TenantPayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paymentRepository: PaymentRepository,
    private val paymentQrRepository: PaymentQrRepository,
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val rentChargeId: Long = checkNotNull(savedStateHandle["rentChargeId"])

    private val _uiState = MutableStateFlow(TenantPayUiState(rentChargeId = rentChargeId))
    val uiState: StateFlow<TenantPayUiState> = _uiState.asStateFlow()

    init {
        loadPaymentData()
    }

    fun loadPaymentData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val qrResult = paymentQrRepository.getActiveQr()
            val billingResult = billingRepository.listCharges()

            val activeQr = (qrResult as? Resource.Success)?.data
            val rentCharges = (billingResult as? Resource.Success)?.data ?: emptyList()
            val matchedCharge = rentCharges.firstOrNull { it.id == rentChargeId }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    activeQr = activeQr,
                    rentCharge = matchedCharge,
                    amountInput = matchedCharge?.balance ?: ""
                )
            }
        }
    }

    fun onAmountChanged(newAmount: String) {
        _uiState.update { it.copy(amountInput = newAmount) }
    }

    fun onUtrChanged(newUtr: String) {
        _uiState.update { it.copy(utrInput = newUtr) }
    }

    fun onImageSelected(uri: Uri?, contentResolver: ContentResolver) {
        if (uri == null) return
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: run {
                _uiState.update { it.copy(errorMessage = "Could not open selected screenshot") }
                return
            }
            val bytes = inputStream.use { it.readBytes() }

            if (bytes.size > 5 * 1024 * 1024) {
                val sizeMb = bytes.size / (1024 * 1024)
                _uiState.update { it.copy(errorMessage = "Selected screenshot is ${sizeMb}MB, exceeding the 5MB limit. Please choose a smaller image.") }
                return
            }

            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val ext = if (mimeType.contains("png")) "png" else if (mimeType.contains("webp")) "webp" else "jpg"
            val fileName = "proof_${System.currentTimeMillis()}.$ext"

            _uiState.update {
                it.copy(
                    selectedImageUri = uri,
                    selectedImageBytes = bytes,
                    selectedImageName = fileName,
                    selectedImageMimeType = mimeType,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Failed to process screenshot: ${e.localizedMessage}") }
        }
    }

    fun onRemoveImage() {
        _uiState.update {
            it.copy(
                selectedImageUri = null,
                selectedImageBytes = null,
                selectedImageName = null,
                selectedImageMimeType = null
            )
        }
    }

    fun submitPayment() {
        val amount = _uiState.value.amountInput.trim()
        val utr = _uiState.value.utrInput.trim()

        if (amount.isBlank() || amount.toBigDecimalOrNull() == null || amount.toBigDecimal() <= BigDecimal.ZERO) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid positive payment amount") }
            return
        }

        if (utr.length < 4) {
            _uiState.update { it.copy(errorMessage = "UTR reference number must be at least 4 characters") }
            return
        }

        val proofPart = _uiState.value.selectedImageBytes?.let { bytes ->
            val mediaType = (_uiState.value.selectedImageMimeType ?: "image/jpeg").toMediaTypeOrNull()
            val requestFile = bytes.toRequestBody(mediaType)
            MultipartBody.Part.createFormData(
                "proof_file",
                _uiState.value.selectedImageName ?: "proof.jpg",
                requestFile
            )
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val request = PaymentSubmissionRequestDto(
                rent_charge_id = rentChargeId,
                amount = amount,
                utr = utr
            )

            when (val result = paymentRepository.submitPayment(request, proofPart)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            paymentSubmitted = true
                        )
                    }
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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
