package com.prajwalhs.learningdashboard.data.auth

import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

/**
 * Mock authentication service standing in for a real login API.
 * Returns a token on success, or null for invalid credentials (the equivalent of HTTP 401).
 */
class FakeAuthDataSource @Inject constructor() {

    suspend fun login(email: String, password: String): String? {
        delay(SIMULATED_LATENCY_MS)
        return if (email == DEMO_EMAIL && password == DEMO_PASSWORD) {
            "demo-token-${UUID.randomUUID()}"
        } else {
            null
        }
    }

    companion object {
        const val DEMO_EMAIL = "student@demo.com"
        const val DEMO_PASSWORD = "password123"
        private const val SIMULATED_LATENCY_MS = 1_000L
    }
}