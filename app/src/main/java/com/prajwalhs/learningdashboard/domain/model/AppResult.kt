package com.prajwalhs.learningdashboard.domain.model

/**
 * Result wrapper for operations that can fail with a known [AppError].
 * Used instead of throwing exceptions across layer boundaries.
 */
sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}