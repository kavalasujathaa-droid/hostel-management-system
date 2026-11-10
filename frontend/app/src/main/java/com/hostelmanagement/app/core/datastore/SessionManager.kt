package com.hostelmanagement.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session_prefs")

sealed interface SessionState {
    data object Loading : SessionState
    data class Authenticated(val session: UserSession) : SessionState
    data object Unauthenticated : SessionState
}

@Singleton
class SessionManager @Inject constructor(
    private val context: Context
) {
    companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val KEY_USER_ID = longPreferencesKey("user_id")
        val KEY_FULL_NAME = stringPreferencesKey("full_name")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_ROLE = stringPreferencesKey("role")
        val KEY_IS_ACTIVE = booleanPreferencesKey("is_active")
    }

    val sessionFlow: Flow<UserSession?> = context.sessionDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val accessToken = prefs[KEY_ACCESS_TOKEN]
            val refreshToken = prefs[KEY_REFRESH_TOKEN]
            val userId = prefs[KEY_USER_ID]
            val fullName = prefs[KEY_FULL_NAME]
            val email = prefs[KEY_EMAIL]
            val role = prefs[KEY_ROLE]
            val isActive = prefs[KEY_IS_ACTIVE] ?: false

            if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank() && userId != null && role != null) {
                UserSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    userId = userId,
                    fullName = fullName ?: "",
                    email = email ?: "",
                    role = role,
                    isActive = isActive
                )
            } else {
                null
            }
        }

    val userSession: Flow<UserSession?> get() = sessionFlow

    val sessionState: Flow<SessionState> = sessionFlow.map { session ->
        if (session != null && session.isLoggedIn) {
            SessionState.Authenticated(session)
        } else {
            SessionState.Unauthenticated
        }
    }

    suspend fun saveSession(session: UserSession) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = session.accessToken
            prefs[KEY_REFRESH_TOKEN] = session.refreshToken
            prefs[KEY_USER_ID] = session.userId
            prefs[KEY_FULL_NAME] = session.fullName
            prefs[KEY_EMAIL] = session.email
            prefs[KEY_ROLE] = session.role
            prefs[KEY_IS_ACTIVE] = session.isActive
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun getAccessToken(): String? {
        return context.sessionDataStore.data.firstOrNull()?.get(KEY_ACCESS_TOKEN)
    }

    suspend fun getRefreshToken(): String? {
        return context.sessionDataStore.data.firstOrNull()?.get(KEY_REFRESH_TOKEN)
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
