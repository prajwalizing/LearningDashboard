package com.prajwalhs.learningdashboard.domain.model

enum class EmailValidationError { EMPTY, INVALID_FORMAT }

enum class PasswordValidationError { EMPTY, TOO_SHORT }

/**
 * Outcome of validating login input. A null error means that field is valid.
 */
data class CredentialsValidation(
    val emailError: EmailValidationError?,
    val passwordError: PasswordValidationError?,
) {
    val isValid: Boolean
        get() = emailError == null && passwordError == null
}