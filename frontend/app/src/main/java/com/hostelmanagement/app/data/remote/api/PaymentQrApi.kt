package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.PaymentQRResponseDto
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PaymentQrApi {

    @GET("payment-qr/active")
    suspend fun getActiveQr(): PaymentQRResponseDto

    @Multipart
    @POST("payment-qr")
    suspend fun uploadQr(@Part file: MultipartBody.Part): PaymentQRResponseDto

    @GET("payment-qr")
    suspend fun listQrs(): List<PaymentQRResponseDto>
}
