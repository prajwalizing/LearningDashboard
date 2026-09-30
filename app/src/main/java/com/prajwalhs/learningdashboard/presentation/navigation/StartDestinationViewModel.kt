package com.prajwalhs.learningdashboard.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalhs.learningdashboard.domain.usecase.HasActiveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Decides the first screen: Dashboard if a session exists, otherwise Login.
 * Null while the session is being read, so the UI shows a loader instead of flashing Login.
 */
@HiltViewModel
class StartDestinationViewModel @Inject constructor(
    private val hasActiveSession: HasActiveSessionUseCase,
) : ViewModel() {

    private val _startDestination = MutableStateFlow<AppRoute?>(null)
    val startDestination: StateFlow<AppRoute?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            _startDestination.value =
                if (hasActiveSession()) AppRoute.Dashboard else AppRoute.Login
        }
    }
}