package com.hostelmanagement.app.domain.repository

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.PaymentResponseDto
import com.hostelmanagement.app.data.remote.dto.PaymentReviewRequestDto
import com.hostelmanagement.app.data.remote.dto.PaymentSubmissionRequestDto

interface PaymentRepository {
    suspend fun submitPayment(request: PaymentSubmissionRequestDto): Resource<PaymentResponseDto>
    suspend fun listPayments(
        tenantId: Long? = null,
        status: String? = null,
        rentChargeId: Long? = null
    ): Resource<List<PaymentResponseDto>>
    suspend fun approvePayment(paymentId: Long): Resource<PaymentResponseDto>
    suspend fun rejectPayment(paymentId: Long, request: PaymentReviewRequestDto): Resource<PaymentResponseDto>
}
