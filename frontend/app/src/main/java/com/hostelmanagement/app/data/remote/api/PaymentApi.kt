package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.PaymentResponseDto
import com.hostelmanagement.app.data.remote.dto.PaymentReviewRequestDto
import com.hostelmanagement.app.data.remote.dto.PaymentSubmissionRequestDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PaymentApi {

    @POST("payments")
    suspend fun submitPayment(@Body request: PaymentSubmissionRequestDto): PaymentResponseDto

    @Multipart
    @POST("payments")
    suspend fun submitPaymentMultipart(
        @Part("rent_charge_id") rentChargeId: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("utr") utr: RequestBody,
        @Part proofFile: MultipartBody.Part?
    ): PaymentResponseDto

    @GET("payments")
    suspend fun listPayments(
        @Query("tenant_id") tenantId: Long? = null,
        @Query("status") status: String? = null,
        @Query("rent_charge_id") rentChargeId: Long? = null
    ): List<PaymentResponseDto>

    @PUT("payments/{payment_id}/approve")
    suspend fun approvePayment(@Path("payment_id") paymentId: Long): PaymentResponseDto

    @PUT("payments/{payment_id}/reject")
    suspend fun rejectPayment(
        @Path("payment_id") paymentId: Long,
        @Body request: PaymentReviewRequestDto
    ): PaymentResponseDto
}
