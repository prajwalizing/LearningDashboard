package com.prajwalhs.learningdashboard.presentation.dashboard

import com.prajwalhs.learningdashboard.domain.model.AppError
import com.prajwalhs.learningdashboard.domain.model.Course

sealed interface DashboardUiState {

    /** No cached data yet and a refresh is in progress (or about to start). */
    data object Loading : DashboardUiState

    /**
     * Courses available (fresh or cached).
     * [refreshError] is non-null when the latest refresh failed and cached data is shown.
     */
    data class Success(
        val courses: List<Course>,
        val isRefreshing: Boolean,
        val refreshError: AppError?,
    ) : DashboardUiState

    /** Refresh succeeded but the server has no courses. */
    data object Empty : DashboardUiState

    /** Refresh failed and there is no cached data to fall back on. */
    data class Error(val error: AppError) : DashboardUiState
}

/** Status of the network refresh, tracked separately from the cached data. */
data class RefreshStatus(
    val isRefreshing: Boolean = false,
    val hasAttempted: Boolean = false,
    val error: AppError? = null,
)

/**
 * Single place that decides what the dashboard shows. Cached data always wins:
 * errors are only shown full-screen when there is nothing cached to display.
 */
fun reduceDashboardState(courses: List<Course>, refresh: RefreshStatus): DashboardUiState = when {
    courses.isNotEmpty() -> DashboardUiState.Success(
        courses = courses,
        isRefreshing = refresh.isRefreshing,
        refreshError = refresh.error,
    )
    refresh.isRefreshing || !refresh.hasAttempted -> DashboardUiState.Loading
    refresh.error != null -> DashboardUiState.Error(refresh.error)
    else -> DashboardUiState.Empty
}