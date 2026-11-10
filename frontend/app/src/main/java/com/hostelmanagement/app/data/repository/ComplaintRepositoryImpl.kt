package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.ComplaintApi
import com.hostelmanagement.app.data.remote.dto.ComplaintCreateDto
import com.hostelmanagement.app.data.remote.dto.ComplaintResponseDto
import com.hostelmanagement.app.data.remote.dto.ComplaintUpdateDto
import com.hostelmanagement.app.domain.repository.ComplaintRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComplaintRepositoryImpl @Inject constructor(
    private val complaintApi: ComplaintApi,
    private val gson: Gson
) : ComplaintRepository {

    override suspend fun createComplaint(request: ComplaintCreateDto): Resource<ComplaintResponseDto> {
        return safeApiCall(gson) { complaintApi.createComplaint(request) }
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
