package com.hostelmanagement.app.core.network

import com.google.gson.Gson
import com.hostelmanagement.app.BuildConfig
import com.hostelmanagement.app.core.datastore.SessionManager
import com.hostelmanagement.app.data.remote.dto.RefreshTokenRequestDto
import com.hostelmanagement.app.data.remote.dto.TokenResponseDto
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
    @Named("unauthenticated") private val unauthenticatedClientProvider: Provider<OkHttpClient>,
    private val gson: Gson
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops if retry fails
        if (responseCount(response) >= 2) {
            return null
        }

        // Do not authenticate auth endpoints
        val path = response.request.url.encodedPath
        if (path.contains("/auth/login") || path.contains("/auth/refresh") || path.contains("/auth/init-owner")) {
            return null
        }

        return runBlocking {
            mutex.withLock {
                val currentToken = sessionManager.getAccessToken()
                val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

                // If token was already refreshed by another thread while this thread was waiting
                if (currentToken != null && currentToken != requestToken) {
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                }

                val refreshToken = sessionManager.getRefreshToken() ?: return@withLock null

                val success = refreshTokens(refreshToken)
                if (success) {
                    val newToken = sessionManager.getAccessToken()
                    if (newToken != null) {
                        return@withLock response.request.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                    }
                }

                // If refresh failed, session is permanently revoked (RFC 6819)
                sessionManager.clearSession()
                null
            }
        }
    }

    private fun refreshTokens(refreshToken: String): Boolean {
        return try {
            val client = unauthenticatedClientProvider.get()
            val requestBody = gson.toJson(RefreshTokenRequestDto(refresh_token = refreshToken))
                .toRequestBody("application/json".toMediaType())

            val refreshUrl = "${BuildConfig.BASE_URL}auth/refresh"
            val request = Request.Builder()
                .url(refreshUrl)
                .post(requestBody)
                .header("Accept", "application/json")
                .build()

            val refreshResponse = client.newCall(request).execute()
            if (refreshResponse.isSuccessful) {
                val bodyString = refreshResponse.body?.string() ?: return false
                val tokenResponse = gson.fromJson(bodyString, TokenResponseDto::class.java)
                runBlocking {
                    sessionManager.updateTokens(
                        accessToken = tokenResponse.access_token,
                        refreshToken = tokenResponse.refresh_token
                    )
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
