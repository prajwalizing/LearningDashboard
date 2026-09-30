package com.prajwalhs.learningdashboard.presentation.login

import com.prajwalhs.learningdashboard.domain.model.AppError
import com.prajwalhs.learningdashboard.domain.model.EmailValidationError
import com.prajwalhs.learningdashboard.domain.model.PasswordValidationError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: EmailValidationError? = null,
    val passwordError: PasswordValidationError? = null,

    /** Field errors are shown only after the first submit, then update live. */
    val hasAttemptedSubmit: Boolean = false,
    val isLoading: Boolean = false,
    /** Error returned by the login call (e.g. invalid credentials). */
    val loginError: AppError? = null,
    /** Navigation modeled as state; the screen navigates when this becomes true. */
    val isLoginSuccessful: Boolean = false,
)