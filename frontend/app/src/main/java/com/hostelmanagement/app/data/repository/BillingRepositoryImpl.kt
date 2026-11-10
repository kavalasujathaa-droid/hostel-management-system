package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.BillingApi
import com.hostelmanagement.app.data.remote.dto.BillingGenerateRequestDto
import com.hostelmanagement.app.data.remote.dto.RentChargeResponseDto
import com.hostelmanagement.app.domain.repository.BillingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val billingApi: BillingApi,
    private val gson: Gson
) : BillingRepository {

    override suspend fun generateBills(request: BillingGenerateRequestDto): Resource<List<RentChargeResponseDto>> {
        return safeApiCall(gson) { billingApi.generateBills(request) }
    }

    override suspend fun listCharges(
        tenantId: Long?,
        year: Int?,
        month: Int?,
        status: String?
    ): Resource<List<RentChargeResponseDto>> {
        return safeApiCall(gson) {
            billingApi.listCharges(tenantId = tenantId, year = year, month = month, status = status)
        }
    }

    override suspend fun getCharge(chargeId: Long): Resource<RentChargeResponseDto> {
        return safeApiCall(gson) { billingApi.getCharge(chargeId) }
    }
}
