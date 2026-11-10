package com.hostelmanagement.app.core.datastore

data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: Long,
    val fullName: String,
    val email: String,
    val role: String,
    val isActive: Boolean
) {
    val isOwner: Boolean get() = role.equals("OWNER", ignoreCase = true)
    val isTenant: Boolean get() = role.equals("TENANT", ignoreCase = true)
    val isLoggedIn: Boolean get() = accessToken.isNotBlank() && userId > 0
}
