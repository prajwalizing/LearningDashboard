package com.prajwalhs.learningdashboard.domain.model

/**
 * Domain-level error categories. The presentation layer maps these to user-facing text.
 */
sealed interface AppError {
    /** No connectivity, timeout, DNS failure, etc. */
    data object Network : AppError

    /** The server responded with a non-2xx status code. */
    data class Server(val code: Int) : AppError

    /** Login rejected by the auth service. */
    data object InvalidCredentials : AppError

    /** Anything unexpected (e.g. malformed response). */
    data object Unknown : AppError
}