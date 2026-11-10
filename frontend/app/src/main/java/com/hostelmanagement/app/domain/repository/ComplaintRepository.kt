package com.hostelmanagement.app.domain.repository

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.ComplaintCreateDto
import com.hostelmanagement.app.data.remote.dto.ComplaintResponseDto
import com.hostelmanagement.app.data.remote.dto.ComplaintUpdateDto

interface ComplaintRepository {
    suspend fun createComplaint(request: ComplaintCreateDto): Resource<ComplaintResponseDto>
    suspend fun listComplaints(status: String? = null): Resource<List<ComplaintResponseDto>>
    suspend fun getComplaint(complaintId: Long): Resource<ComplaintResponseDto>
    suspend fun updateComplaintStatus(complaintId: Long, request: ComplaintUpdateDto): Resource<ComplaintResponseDto>
}
