package com.prajwalhs.learningdashboard.presentation.dashboard


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalhs.learningdashboard.R
import com.prajwalhs.learningdashboard.domain.model.AppError
import com.prajwalhs.learningdashboard.domain.model.Course
import com.prajwalhs.learningdashboard.presentation.components.EmptyView
import com.prajwalhs.learningdashboard.presentation.components.ErrorView
import com.prajwalhs.learningdashboard.presentation.components.LoadingView
import com.prajwalhs.learningdashboard.presentation.components.OfflineBanner
import com.prajwalhs.learningdashboard.presentation.theme.LearningDashboardTheme
import com.prajwalhs.learningdashboard.presentation.util.toUserMessage

@Composable
fun DashboardScreen(
    onCourseClick: (courseId: Int) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        uiState = uiState,
        onCourseClick = onCourseClick,
        onRefresh = viewModel::onRefresh,
        onRetryClick = viewModel::onRetryClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onCourseClick: (courseId: Int) -> Unit,
    onRefresh: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.dashboard_title)) }) },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (uiState) {
            DashboardUiState.Loading -> LoadingView(modifier = contentModifier)

            is DashboardUiState.Error -> ErrorView(
                message = uiState.error.toUserMessage(),
                onRetry = onRetryClick,
                modifier = contentModifier,
            )

            DashboardUiState.Empty -> EmptyView(
                title = stringResource(R.string.empty_courses_title),
                message = stringResource(R.string.empty_courses_message),
                actionLabel = stringResource(R.string.action_refresh),
                onAction = onRefresh,
                modifier = contentModifier,
            )

            is DashboardUiState.Success -> CourseList(
                state = uiState,
                onCourseClick = onCourseClick,
                onRefresh = onRefresh,
                modifier = contentModifier,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseList(
    state: DashboardUiState.Success,
    onCourseClick: (courseId: Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        when (val error = state.refreshError) {
            null -> Unit
            AppError.Network -> OfflineBanner()
            else -> RefreshErrorBanner(message = error.toUserMessage())
        }

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Stable keys: Compose reuses items correctly when progress changes.
                items(items = state.courses, key = { it.id }) { course ->
                    CourseCard(course = course, onClick = { onCourseClick(course.id) })
                }
            }
        }
    }
}

/** Shown above cached data when a refresh failed for a non-network reason (e.g. server error). */
@Composable
private fun RefreshErrorBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

// ---------- Previews ----------

private val previewCourses = listOf(
    Course(1, "Python Programming", "John Smith", totalLessons = 20, completedLessons = 13),
    Course(2, "Generative AI", "Sarah Williams", totalLessons = 16, completedLessons = 6),
    Course(3, "Full Stack Development", "David Brown", totalLessons = 28, completedLessons = 7),
)

@Preview(showBackground = true)
@Composable
private fun DashboardSuccessPreview() {
    LearningDashboardTheme {
        DashboardContent(
            uiState = DashboardUiState.Success(previewCourses, isRefreshing = false, refreshError = null),
            onCourseClick = {}, onRefresh = {}, onRetryClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardOfflinePreview() {
    LearningDashboardTheme {
        DashboardContent(
            uiState = DashboardUiState.Success(previewCourses, isRefreshing = false, refreshError = AppError.Network),
            onCourseClick = {}, onRefresh = {}, onRetryClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardErrorPreview() {
    LearningDashboardTheme {
        DashboardContent(
            uiState = DashboardUiState.Error(AppError.Network),
            onCourseClick = {}, onRefresh = {}, onRetryClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardEmptyPreview() {
    LearningDashboardTheme {
        DashboardContent(
            uiState = DashboardUiState.Empty,
            onCourseClick = {}, onRefresh = {}, onRetryClick = {},
        )
    }
}