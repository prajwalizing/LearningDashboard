package com.prajwalhs.learningdashboard.domain.usecase

import com.prajwalhs.learningdashboard.domain.repository.AuthRepository
import javax.inject.Inject

class HasActiveSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean = authRepository.hasActiveSession()
}