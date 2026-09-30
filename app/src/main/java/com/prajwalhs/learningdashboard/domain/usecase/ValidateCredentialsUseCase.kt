package com.prajwalhs.learningdashboard.domain.usecase

import com.prajwalhs.learningdashboard.domain.model.CredentialsValidation
import com.prajwalhs.learningdashboard.domain.model.EmailValidationError
import com.prajwalhs.learningdashboard.domain.model.PasswordValidationError
import javax.inject.Inject

/**
 * Validates login input. Uses a plain Kotlin regex instead of android.util.Patterns
 * so the rules can be unit-tested without Android.
 */
class ValidateCredentialsUseCase @Inject constructor() {

    operator fun invoke(email: String, password: String): CredentialsValidation {
        val trimmedEmail = email.trim()

        val emailError = when {
            trimmedEmail.isEmpty() -> EmailValidationError.EMPTY
            !EMAIL_REGEX.matches(trimmedEmail) -> EmailValidationError.INVALID_FORMAT
            else -> null
        }

        val passwordError = when {
            password.isEmpty() -> PasswordValidationError.EMPTY
            password.length < MIN_PASSWORD_LENGTH -> PasswordValidationError.TOO_SHORT
            else -> null
        }

        return CredentialsValidation(emailError = emailError, passwordError = passwordError)
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}