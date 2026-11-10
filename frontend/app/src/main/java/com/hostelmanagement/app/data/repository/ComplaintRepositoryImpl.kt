package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.ComplaintApi
import com.hostelmanagement.app.data.remote.dto.ComplaintCreateDto
import com.hostelmanagement.app.data.remote.dto.ComplaintResponseDto
import com.hostelmanagement.app.data.remote.dto.ComplaintUpdateDto
import com.hostelmanagement.app.domain.repository.ComplaintRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComplaintRepositoryImpl @Inject constructor(
    private val complaintApi: ComplaintApi,
    private val gson: Gson
) : ComplaintRepository {

    override suspend fun createComplaint(
        request: ComplaintCreateDto,
        attachmentPart: MultipartBody.Part?
    ): Resource<ComplaintResponseDto> {
        return safeApiCall(gson) {
            if (attachmentPart != null) {
                val titleBody = request.title.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryBody = request.category.toRequestBody("text/plain".toMediaTypeOrNull())
                val descBody = request.description.toRequestBody("text/plain".toMediaTypeOrNull())
                val stayIdBody = request.tenant_stay_id?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                complaintApi.createComplaintMultipart(
                    title = titleBody,
                    category = categoryBody,
                    description = descBody,
                    tenantStayId = stayIdBody,
                    attachment = attachmentPart
                )
            } else {
                complaintApi.createComplaint(request)
            }
        }
    }

    override suspend fun listComplaints(status: String?): Resource<List<ComplaintResponseDto>> {
        return safeApiCall(gson) { complaintApi.listComplaints(status = status) }
    }

    override suspend fun getComplaint(complaintId: Long): Resource<ComplaintResponseDto> {
        return safeApiCall(gson) { complaintApi.getComplaint(complaintId) }
    }

    override suspend fun updateComplaintStatus(
        complaintId: Long,
        request: ComplaintUpdateDto
    ): Resource<ComplaintResponseDto> {
        return safeApiCall(gson) { complaintApi.updateComplaintStatus(complaintId, request) }
    }
}
