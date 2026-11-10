package com.hostelmanagement.app.ui.tenant.complaints

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.ComplaintCreateDto
import com.hostelmanagement.app.domain.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

val COMPLAINT_CATEGORIES = listOf("Plumbing", "Electrical", "Cleaning", "Maintenance", "Other")

data class TenantRaiseComplaintUiState(
    val title: String = "",
    val category: String = "Plumbing",
    val description: String = "",
    val selectedImageUri: Uri? = null,
    val selectedImageBytes: ByteArray? = null,
    val selectedImageName: String? = null,
    val selectedImageMimeType: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TenantRaiseComplaintUiState

        if (title != other.title) return false
        if (category != other.category) return false
        if (description != other.description) return false
        if (selectedImageUri != other.selectedImageUri) return false
        if (selectedImageBytes != null) {
            if (other.selectedImageBytes == null) return false
            if (!selectedImageBytes.contentEquals(other.selectedImageBytes)) return false
        } else if (other.selectedImageBytes != null) return false
        if (selectedImageName != other.selectedImageName) return false
        if (selectedImageMimeType != other.selectedImageMimeType) return false
        if (isSubmitting != other.isSubmitting) return false
        if (errorMessage != other.errorMessage) return false
        if (isSuccess != other.isSuccess) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (selectedImageUri?.hashCode() ?: 0)
        result = 31 * result + (selectedImageBytes?.contentHashCode() ?: 0)
        result = 31 * result + (selectedImageName?.hashCode() ?: 0)
        result = 31 * result + (selectedImageMimeType?.hashCode() ?: 0)
        result = 31 * result + isSubmitting.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + isSuccess.hashCode()
        return result
    }
}

@HiltViewModel
class TenantRaiseComplaintViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantRaiseComplaintUiState())
    val uiState: StateFlow<TenantRaiseComplaintUiState> = _uiState.asStateFlow()

    fun onTitleChanged(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, errorMessage = null) }
    }

    fun onCategoryChanged(newCategory: String) {
        _uiState.update { it.copy(category = newCategory, errorMessage = null) }
    }

    fun onDescriptionChanged(newDescription: String) {
        _uiState.update { it.copy(description = newDescription, errorMessage = null) }
    }

    fun onImageSelected(uri: Uri?, contentResolver: ContentResolver) {
        if (uri == null) return
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: run {
                _uiState.update { it.copy(errorMessage = "Could not open selected image") }
                return
            }
            val bytes = inputStream.use { it.readBytes() }

            if (bytes.size > 5 * 1024 * 1024) {
                val sizeMb = bytes.size / (1024 * 1024)
                _uiState.update { it.copy(errorMessage = "Selected image is ${sizeMb}MB, exceeding the 5MB limit. Please choose a smaller image.") }
                return
            }

            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val ext = if (mimeType.contains("png")) "png" else if (mimeType.contains("webp")) "webp" else "jpg"
            val fileName = "complaint_${System.currentTimeMillis()}.$ext"

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
            _uiState.update { it.copy(errorMessage = "Failed to process image: ${e.localizedMessage}") }
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

    fun submitComplaint() {
        val title = _uiState.value.title.trim()
        val category = _uiState.value.category.trim()
        val description = _uiState.value.description.trim()

        if (title.length < 3) {
            _uiState.update { it.copy(errorMessage = "Title must be at least 3 characters") }
            return
        }
        if (title.length > 150) {
            _uiState.update { it.copy(errorMessage = "Title must not exceed 150 characters") }
            return
        }
        if (description.length < 5) {
            _uiState.update { it.copy(errorMessage = "Description must be at least 5 characters") }
            return
        }

        val attachmentPart = _uiState.value.selectedImageBytes?.let { bytes ->
            val mediaType = (_uiState.value.selectedImageMimeType ?: "image/jpeg").toMediaTypeOrNull()
            val requestFile = bytes.toRequestBody(mediaType)
            MultipartBody.Part.createFormData(
                "attachment",
                _uiState.value.selectedImageName ?: "attachment.jpg",
                requestFile
            )
        }

        val request = ComplaintCreateDto(
            title = title,
            category = category,
            tenant_stay_id = null,
            description = description
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = complaintRepository.createComplaint(request, attachmentPart)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isSuccess = true
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.message ?: "Failed to submit complaint"
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
