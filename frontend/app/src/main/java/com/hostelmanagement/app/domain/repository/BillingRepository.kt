package com.hostelmanagement.app.domain.repository

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.BillingGenerateRequestDto
import com.hostelmanagement.app.data.remote.dto.RentChargeResponseDto

interface BillingRepository {
    suspend fun generateBills(request: BillingGenerateRequestDto): Resource<List<RentChargeResponseDto>>
    suspend fun listCharges(
        tenantId: Long? = null,
        year: Int? = null,
        month: Int? = null,
        status: String? = null
    ): Resource<List<RentChargeResponseDto>>
    suspend fun getCharge(chargeId: Long): Resource<RentChargeResponseDto>
}
