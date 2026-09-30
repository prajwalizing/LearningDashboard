package com.prajwalhs.learningdashboard.domain.usecase

import com.prajwalhs.learningdashboard.domain.model.AppResult
import com.prajwalhs.learningdashboard.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Normalizes the email (trim + lowercase) before authenticating,
 * so " Student@Demo.com " and "student@demo.com" are treated the same.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): AppResult<Unit> =
        authRepository.login(email = email.trim().lowercase(), password = password)
}