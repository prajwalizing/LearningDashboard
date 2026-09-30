package com.prajwalhs.learningdashboard.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.prajwalhs.learningdashboard.R
import com.prajwalhs.learningdashboard.domain.model.AppError

/**
 * Maps a domain error to user-facing text. Kept in the UI layer so ViewModels
 * stay free of Android resources and Context.
 */
@Composable
fun AppError.toUserMessage(): String = when (this) {
    AppError.Network -> stringResource(R.string.error_network)
    is AppError.Server -> stringResource(R.string.error_server, code)
    AppError.InvalidCredentials -> stringResource(R.string.error_invalid_credentials)
    AppError.Unknown -> stringResource(R.string.error_unknown)
}