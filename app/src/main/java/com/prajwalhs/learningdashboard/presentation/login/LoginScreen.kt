package com.prajwalhs.learningdashboard.presentation.login


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalhs.learningdashboard.R
import com.prajwalhs.learningdashboard.domain.model.AppError
import com.prajwalhs.learningdashboard.domain.model.EmailValidationError
import com.prajwalhs.learningdashboard.domain.model.PasswordValidationError
import com.prajwalhs.learningdashboard.domain.usecase.ValidateCredentialsUseCase
import com.prajwalhs.learningdashboard.presentation.theme.LearningDashboardTheme
import com.prajwalhs.learningdashboard.presentation.util.toUserMessage

/**
 * Stateful entry point: wires the ViewModel and handles navigation.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) currentOnLoginSuccess()
    }

    LoginContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onLoginClick,
    )
}

/**
 * Stateless UI: renders [uiState] and reports events. Previewable without Hilt.
 */
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Pure UI concern, so it lives here rather than in the ViewModel.
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val isInputEnabled = !uiState.isLoading

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.label_email)) },
                singleLine = true,
                enabled = isInputEnabled,
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { error -> { Text(error.toMessage()) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.label_password)) },
                singleLine = true,
                enabled = isInputEnabled,
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError?.let { error -> { Text(error.toMessage()) } },
                visualTransformation = if (isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Text(
                            stringResource(
                                if (isPasswordVisible) R.string.action_hide_password
                                else R.string.action_show_password
                            )
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onLoginClick()
                    },
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            uiState.loginError?.let { error ->
                Text(
                    text = error.toUserMessage(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onLoginClick()
                },
                enabled = isInputEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.action_login))
                }
            }

            Text(
                text = stringResource(R.string.login_demo_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmailValidationError.toMessage(): String = when (this) {
    EmailValidationError.EMPTY -> stringResource(R.string.error_email_empty)
    EmailValidationError.INVALID_FORMAT -> stringResource(R.string.error_email_invalid)
}

@Composable
private fun PasswordValidationError.toMessage(): String = when (this) {
    PasswordValidationError.EMPTY -> stringResource(R.string.error_password_empty)
    PasswordValidationError.TOO_SHORT -> stringResource(
        R.string.error_password_too_short,
        ValidateCredentialsUseCase.MIN_PASSWORD_LENGTH,
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginContentPreview() {
    LearningDashboardTheme {
        LoginContent(uiState = LoginUiState(), onEmailChange = {}, onPasswordChange = {}, onLoginClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentErrorPreview() {
    LearningDashboardTheme {
        LoginContent(
            uiState = LoginUiState(
                email = "student@",
                password = "123",
                hasAttemptedSubmit = true,
                emailError = EmailValidationError.INVALID_FORMAT,
                passwordError = PasswordValidationError.TOO_SHORT,
                loginError = AppError.InvalidCredentials,
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
        )
    }
}