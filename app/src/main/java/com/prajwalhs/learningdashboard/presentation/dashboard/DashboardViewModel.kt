package com.prajwalhs.learningdashboard.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalhs.learningdashboard.domain.model.AppResult
import com.prajwalhs.learningdashboard.domain.usecase.ObserveCoursesUseCase
import com.prajwalhs.learningdashboard.domain.usecase.RefreshCoursesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeCourses: ObserveCoursesUseCase,
    private val refreshCourses: RefreshCoursesUseCase,
) : ViewModel() {

    private val refreshStatus = MutableStateFlow(RefreshStatus())

    val uiState: StateFlow<DashboardUiState> =
        combine(observeCourses(), refreshStatus, ::reduceDashboardState)
            .stateIn(
                scope = viewModelScope,
                // Keep the Room query alive across configuration changes (e.g. rotation).
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = DashboardUiState.Loading,
            )

    init {
        refresh()
    }

    fun onRefresh() = refresh()

    fun onRetryClick() = refresh()

    private fun refresh() {
        if (refreshStatus.value.isRefreshing) return // ignore overlapping refreshes

        viewModelScope.launch {
            // Keep the previous error while retrying so the banner does not flicker.
            refreshStatus.update { it.copy(isRefreshing = true) }
            val result = refreshCourses()
            refreshStatus.update {
                RefreshStatus(
                    isRefreshing = false,
                    hasAttempted = true,
                    error = (result as? AppResult.Failure)?.error,
                )
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}