package com.hostelmanagement.app.data.repository

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.core.network.safeApiCall
import com.hostelmanagement.app.data.remote.api.OwnerApi
import com.hostelmanagement.app.data.remote.dto.OwnerCreateRequestDto
import com.hostelmanagement.app.data.remote.dto.UserResponseDto
import com.hostelmanagement.app.domain.repository.OwnerRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnerRepositoryImpl @Inject constructor(
    private val ownerApi: OwnerApi,
    private val gson: Gson
) : OwnerRepository {

    override suspend fun listOwners(): Resource<List<UserResponseDto>> {
        return safeApiCall(gson) { ownerApi.listOwners() }
    }

    override suspend fun createOwner(request: OwnerCreateRequestDto): Resource<UserResponseDto> {
        return safeApiCall(gson) { ownerApi.createOwner(request) }
    }
}
