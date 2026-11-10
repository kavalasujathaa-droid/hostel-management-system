package com.hostelmanagement.app.ui.owner.qr

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.PaymentQRResponseDto
import com.hostelmanagement.app.domain.repository.PaymentQrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class OwnerPaymentQrUiState(
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val activeQr: PaymentQRResponseDto? = null,
    val qrHistory: List<PaymentQRResponseDto> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class OwnerPaymentQrViewModel @Inject constructor(
    private val paymentQrRepository: PaymentQrRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerPaymentQrUiState())
    val uiState: StateFlow<OwnerPaymentQrUiState> = _uiState.asStateFlow()

    init {
        loadQrData()
    }

    fun loadQrData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val activeResult = paymentQrRepository.getActiveQr()
            val historyResult = paymentQrRepository.listQrs()

            val active = (activeResult as? Resource.Success)?.data
            val history = (historyResult as? Resource.Success)?.data ?: emptyList()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    activeQr = active,
                    qrHistory = history,
                    errorMessage = if (active == null && history.isEmpty() && activeResult is Resource.Error) {
                        null // No QR configured initially is a normal empty state
                    } else null
                )
            }
        }
    }

    fun uploadQrFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }
            try {
                val file = createTempFileFromUri(context, uri)
                if (file == null) {
                    _uiState.update { it.copy(isUploading = false, errorMessage = "Failed to read image file") }
                    return@launch
                }

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

                when (val result = paymentQrRepository.uploadQr(multipartBody)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                activeQr = result.data,
                                successMessage = "New UPI QR code activated successfully"
                            )
                        }
                        loadQrData()
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isUploading = false, errorMessage = result.message) }
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, errorMessage = e.localizedMessage ?: "Upload failed") }
            }
        }
    }

    private fun createTempFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, "qr_upload_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(tempFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }
}
