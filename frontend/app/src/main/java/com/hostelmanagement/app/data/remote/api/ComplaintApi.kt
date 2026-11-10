package com.hostelmanagement.app.data.remote.api

import com.hostelmanagement.app.data.remote.dto.ComplaintCreateDto
import com.hostelmanagement.app.data.remote.dto.ComplaintResponseDto
import com.hostelmanagement.app.data.remote.dto.ComplaintUpdateDto
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

interface ComplaintApi {

    @POST("complaints")
    suspend fun createComplaint(@Body request: ComplaintCreateDto): ComplaintResponseDto

    @Multipart
    @POST("complaints")
    suspend fun createComplaintMultipart(
        @Part("title") title: RequestBody,
        @Part("category") category: RequestBody,
        @Part("description") description: RequestBody,
        @Part("tenant_stay_id") tenantStayId: RequestBody?,
        @Part attachment: MultipartBody.Part?
    ): ComplaintResponseDto

    @GET("complaints")
    suspend fun listComplaints(@Query("status") status: String? = null): List<ComplaintResponseDto>

    @GET("complaints/{complaint_id}")
    suspend fun getComplaint(@Path("complaint_id") complaintId: Long): ComplaintResponseDto

    @PUT("complaints/{complaint_id}")
    suspend fun updateComplaintStatus(
        @Path("complaint_id") complaintId: Long,
        @Body request: ComplaintUpdateDto
    ): ComplaintResponseDto
}
