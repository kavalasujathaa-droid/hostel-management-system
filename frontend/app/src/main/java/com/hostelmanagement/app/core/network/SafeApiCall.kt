package com.hostelmanagement.app.core.network

import com.google.gson.Gson
import com.hostelmanagement.app.core.common.Resource
import com.hostelmanagement.app.data.remote.dto.ErrorResponseDto
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(
    gson: Gson = Gson(),
    apiCall: suspend () -> T
): Resource<T> {
    return try {
        val result = apiCall()
        Resource.Success(result)
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val errorDto = try {
            if (!errorBody.isNullOrBlank()) {
                gson.fromJson(errorBody, ErrorResponseDto::class.java)
            } else {
                null
            }
        } catch (jsonEx: Exception) {
            null
        }

        val code = errorDto?.error?.code ?: "HTTP_${e.code()}"
        val message = errorDto?.error?.message
            ?: when (e.code()) {
                401 -> "Session expired or credentials invalid"
                403 -> "Permission denied"
                404 -> "Resource not found"
                409 -> "Conflict: Record already exists or state invalid"
                422 -> "Validation failed for request"
                503 -> "Database or service unavailable"
                else -> e.message() ?: "A network error occurred (${e.code()})"
            }

        Resource.Error(message = message, code = code)
    } catch (e: IOException) {
        Resource.Error(message = "Network connection failed. Please check your internet or server.", code = "NETWORK_ERROR")
    } catch (e: Exception) {
        Resource.Error(message = e.localizedMessage ?: "An unexpected error occurred", code = "UNKNOWN_ERROR")
    }
}
