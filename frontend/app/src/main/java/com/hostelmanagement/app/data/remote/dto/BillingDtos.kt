package com.hostelmanagement.app.data.remote.dto

data class BillingGenerateRequestDto(
    val year: Int,
    val month: Int
)

data class RentChargeResponseDto(
    val id: Long,
    val tenant_stay_id: Long,
    val tenant_name: String? = null,
    val room_number: String? = null,
    val billing_year: Int,
    val billing_month: Int,
    val period_start: String,
    val period_end: String,
    val monthly_rent_snapshot: String,
    val days_in_month: Int,
    val billable_days: Int,
    val amount_due: String,
    val amount_paid: String,
    val balance: String,
    val due_date: String,
    val status: String,
    val created_at: String
)
