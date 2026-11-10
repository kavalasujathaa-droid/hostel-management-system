package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.HealthReadyDto
import com.hostelmanagement.app.data.remote.dto.HealthStatusDto
import retrofit2.http.GET

interface HealthApi {

    @GET("health/live")
    suspend fun checkLiveness(): HealthStatusDto

    @GET("health/ready")
    suspend fun checkReadiness(): HealthReadyDto
}
