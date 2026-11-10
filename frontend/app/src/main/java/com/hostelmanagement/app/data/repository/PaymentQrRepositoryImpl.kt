package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.PaymentQrApi
import com.hostelmanagement.app.data.remote.dto.PaymentQRResponseDto
import com.hostelmanagement.app.domain.repository.PaymentQrRepository
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentQrRepositoryImpl @Inject constructor(
    private val paymentQrApi: PaymentQrApi,
    private val gson: Gson
) : PaymentQrRepository {

    override suspend fun getActiveQr(): Resource<PaymentQRResponseDto> {
        return safeApiCall(gson) { paymentQrApi.getActiveQr() }
    }

    override suspend fun uploadQr(file: MultipartBody.Part): Resource<PaymentQRResponseDto> {
        return safeApiCall(gson) { paymentQrApi.uploadQr(file) }
    }

    override suspend fun listQrs(): Resource<List<PaymentQRResponseDto>> {
        return safeApiCall(gson) { paymentQrApi.listQrs() }
    }
}
