package com.prajwalhs.learningdashboard.domain.repository

import com.prajwalhs.learningdashboard.domain.model.AppResult

interface AuthRepository {

    /** Authenticates the user and persists the session on success. */
    suspend fun login(email: String, password: String): AppResult<Unit>

    /** True if a session token is stored, so the login screen can be skipped. */
    suspend fun hasActiveSession(): Boolean
}