package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.PaymentApi
import com.hostelmanagement.app.data.remote.dto.PaymentResponseDto
import com.hostelmanagement.app.data.remote.dto.PaymentReviewRequestDto
import com.hostelmanagement.app.data.remote.dto.PaymentSubmissionRequestDto
import com.hostelmanagement.app.domain.repository.PaymentRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val paymentApi: PaymentApi,
    private val gson: Gson
) : PaymentRepository {

    override suspend fun submitPayment(
        request: PaymentSubmissionRequestDto,
        proofFilePart: MultipartBody.Part?
    ): Resource<PaymentResponseDto> {
        return safeApiCall(gson) {
            if (proofFilePart != null) {
                val rentIdBody = request.rent_charge_id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val amountBody = request.amount.toRequestBody("text/plain".toMediaTypeOrNull())
                val utrBody = request.utr.toRequestBody("text/plain".toMediaTypeOrNull())
                paymentApi.submitPaymentMultipart(
                    rentChargeId = rentIdBody,
                    amount = amountBody,
                    utr = utrBody,
                    proofFile = proofFilePart
                )
            } else {
                paymentApi.submitPayment(request)
            }
        }
    }

    override suspend fun listPayments(
        tenantId: Long?,
        status: String?,
        rentChargeId: Long?
    ): Resource<List<PaymentResponseDto>> {
        return safeApiCall(gson) {
            paymentApi.listPayments(tenantId = tenantId, status = status, rentChargeId = rentChargeId)
        }
    }

    override suspend fun approvePayment(paymentId: Long): Resource<PaymentResponseDto> {
        return safeApiCall(gson) { paymentApi.approvePayment(paymentId) }
    }

    override suspend fun rejectPayment(paymentId: Long, request: PaymentReviewRequestDto): Resource<PaymentResponseDto> {
        return safeApiCall(gson) { paymentApi.rejectPayment(paymentId, request) }
    }
}
