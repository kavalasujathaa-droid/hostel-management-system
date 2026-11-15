package com.hostelmanagement.app.domain.repository

import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.OwnerCreateRequestDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto

interface OwnerRepository {
    suspend fun listOwners(): Resource<List<UserResponseDto>>
    suspend fun createOwner(request: OwnerCreateRequestDto): Resource<UserResponseDto>
}
