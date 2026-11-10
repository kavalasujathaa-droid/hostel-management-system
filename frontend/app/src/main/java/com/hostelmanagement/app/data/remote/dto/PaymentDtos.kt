package com.hostelmanagement.app.data.remote.dto

data class PaymentSubmissionRequestDto(
    val rent_charge_id: Long,
    val amount: String,
    val utr: String
)

data class PaymentReviewRequestDto(
    val rejection_reason: String? = null
)

data class PaymentResponseDto(
    val id: Long,
    val rent_charge_id: Long,
    val submitted_by: Long,
    val submitted_by_name: String? = null,
    val reviewed_by: Long? = null,
    val amount: String,
    val utr: String,
    val status: String,
    val rejection_reason: String? = null,
    val submitted_at: String,
    val reviewed_at: String? = null
)
