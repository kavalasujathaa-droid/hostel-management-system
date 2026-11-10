package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.BillingGenerateRequestDto
import com.hostelmanagement.app.data.remote.dto.RentChargeResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BillingApi {

    @POST("billing/generate")
    suspend fun generateBills(@Body request: BillingGenerateRequestDto): List<RentChargeResponseDto>

    @GET("billing/charges")
    suspend fun listCharges(
        @Query("tenant_id") tenantId: Long? = null,
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null,
        @Query("status") status: String? = null
    ): List<RentChargeResponseDto>

    @GET("billing/charges/{charge_id}")
    suspend fun getCharge(@Path("charge_id") chargeId: Long): RentChargeResponseDto
}
