package com.hostelmanagement.app.domain.repository

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.PaymentQRResponseDto
import okhttp3.MultipartBody

interface PaymentQrRepository {
    suspend fun getActiveQr(): Resource<PaymentQRResponseDto>
    suspend fun uploadQr(file: MultipartBody.Part): Resource<PaymentQRResponseDto>
    suspend fun listQrs(): Resource<List<PaymentQRResponseDto>>
}
