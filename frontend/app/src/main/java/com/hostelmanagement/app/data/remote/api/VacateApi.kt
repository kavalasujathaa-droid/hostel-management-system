package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.VacateRequestCreateDto
import com.hostelmanagement.app.data.remote.dto.VacateRequestResponseDto
import com.hostelmanagement.app.data.remote.dto.VacateRequestReviewDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VacateApi {

    @POST("vacate-requests")
    suspend fun submitVacateRequest(@Body request: VacateRequestCreateDto): VacateRequestResponseDto

    @GET("vacate-requests")
    suspend fun listVacateRequests(@Query("status") status: String? = null): List<VacateRequestResponseDto>

    @PUT("vacate-requests/{request_id}/review")
    suspend fun reviewVacateRequest(
        @Path("request_id") requestId: Long,
        @Body request: VacateRequestReviewDto
    ): VacateRequestResponseDto
}
