package com.prajwalhs.learningdashboard.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalhs.learningdashboard.domain.model.AppResult
import com.prajwalhs.learningdashboard.domain.usecase.LoginUseCase
import com.prajwalhs.learningdashboard.domain.usecase.ValidateCredentialsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val validateCredentials: ValidateCredentialsUseCase,
    private val login: LoginUseCase,
) : ViewModel() {

    // Intentionally not backed by SavedStateHandle: the password must not be
    // written to saved instance state.
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { state ->
            state.copy(
                email = email,
                emailError = if (state.hasAttemptedSubmit) {
                    validateCredentials(email, state.password).emailError
                } else {
                    null
                },
                loginError = null,
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { state ->
            state.copy(
                password = password,
                passwordError = if (state.hasAttemptedSubmit) {
                    validateCredentials(state.email, password).passwordError
                } else {
                    null
                },
                loginError = null,
            )
        }
    }

    fun onLoginClick() {
        val current = _uiState.value
        if (current.isLoading) return // ignore double taps

        val validation = validateCredentials(current.email, current.password)
        _uiState.update {
            it.copy(
                hasAttemptedSubmit = true,
                emailError = validation.emailError,
                passwordError = validation.passwordError,
                loginError = null,
            )
        }
        if (!validation.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = login(current.email, current.password)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, isLoginSuccessful = true)
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, loginError = result.error)
                }
            }
        }
    }
}