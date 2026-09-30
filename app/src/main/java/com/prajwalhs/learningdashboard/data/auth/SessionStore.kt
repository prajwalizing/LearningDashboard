package com.prajwalhs.learningdashboard.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the auth session.
 *
 * NOTE: For this assignment the token is stored in plain DataStore.
 * In production, it would be encrypted with a key held in the Android Keystore
 * (e.g. DataStore + Tink), paired with short-lived access tokens and refresh tokens.
 */
@Singleton
class SessionStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences -> preferences[KEY_AUTH_TOKEN] = token }
    }

    suspend fun getToken(): String? =
        dataStore.data
            .catch { error ->
                // A corrupted or unreadable file should log the user out, not crash the app.
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { preferences -> preferences[KEY_AUTH_TOKEN] }
            .first()

    suspend fun clear() {
        dataStore.edit { preferences -> preferences.remove(KEY_AUTH_TOKEN) }
    }

    private companion object {
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
    }
}