package com.hostelmanagement.app.core.network

import java.io.IOException

open class ApiException(
    val errorCode: String,
    override val message: String,
    val statusCode: Int = 400
) : IOException(message)

class UnauthorizedException(
    message: String = "Session expired or invalid. Please sign in again."
) : ApiException(errorCode = "UNAUTHORIZED", message = message, statusCode = 401)

class ForbiddenException(
    message: String = "You do not have permission to perform this action."
) : ApiException(errorCode = "FORBIDDEN", message = message, statusCode = 403)

class NotFoundException(
    message: String = "The requested resource was not found."
) : ApiException(errorCode = "NOT_FOUND", message = message, statusCode = 404)

class ConflictException(
    message: String = "A resource conflict occurred."
) : ApiException(errorCode = "CONFLICT", message = message, statusCode = 409)

class ValidationException(
    message: String = "Validation error in request."
) : ApiException(errorCode = "VALIDATION_ERROR", message = message, statusCode = 422)

class ServerUnavailableException(
    message: String = "The service or database is currently unavailable. Please try again later."
) : ApiException(errorCode = "DATABASE_UNAVAILABLE", message = message, statusCode = 503)
