package com.prajwalhs.learningdashboard.data.repository

import com.prajwalhs.learningdashboard.data.auth.FakeAuthDataSource
import com.prajwalhs.learningdashboard.data.auth.SessionStore
import com.prajwalhs.learningdashboard.data.mapper.toAppError
import com.prajwalhs.learningdashboard.domain.model.AppError
import com.prajwalhs.learningdashboard.domain.model.AppResult
import com.prajwalhs.learningdashboard.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FakeAuthDataSource,
    private val sessionStore: SessionStore,
) : AuthRepository {

    override suspend fun login(email: String, password: String): AppResult<Unit> {
        return try {
            val token = authDataSource.login(email, password)
                ?: return AppResult.Failure(AppError.InvalidCredentials)
            sessionStore.saveToken(token)
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e // never swallow coroutine cancellation
        } catch (e: Exception) {
            AppResult.Failure(e.toAppError())
        }
    }

    override suspend fun hasActiveSession(): Boolean =
        !sessionStore.getToken().isNullOrBlank()
}