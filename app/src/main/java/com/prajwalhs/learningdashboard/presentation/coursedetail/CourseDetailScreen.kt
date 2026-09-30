package com.prajwalhs.learningdashboard.presentation.coursedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalhs.learningdashboard.R
import com.prajwalhs.learningdashboard.domain.model.Course
import com.prajwalhs.learningdashboard.domain.model.CourseDetail
import com.prajwalhs.learningdashboard.domain.model.Lesson
import com.prajwalhs.learningdashboard.presentation.components.CourseProgressBar
import com.prajwalhs.learningdashboard.presentation.components.EmptyView
import com.prajwalhs.learningdashboard.presentation.components.LoadingView
import com.prajwalhs.learningdashboard.presentation.theme.LearningDashboardTheme

@Composable
fun CourseDetailScreen(
    onBackClick: () -> Unit,
    viewModel: CourseDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CourseDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onLessonCompleteClick = viewModel::onLessonCompleteClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailContent(
    uiState: CourseDetailUiState,
    onBackClick: () -> Unit,
    onLessonCompleteClick: (lessonId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? CourseDetailUiState.Success)?.detail?.course?.title.orEmpty()
                    Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (uiState) {
            CourseDetailUiState.Loading -> LoadingView(modifier = contentModifier)

            CourseDetailUiState.NotFound -> EmptyView(
                title = stringResource(R.string.course_not_found_title),
                message = stringResource(R.string.course_not_found_message),
                actionLabel = stringResource(R.string.action_go_back),
                onAction = onBackClick,
                modifier = contentModifier,
            )

            is CourseDetailUiState.Success -> LessonList(
                detail = uiState.detail,
                onLessonCompleteClick = onLessonCompleteClick,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun LessonList(
    detail: CourseDetail,
    onLessonCompleteClick: (lessonId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item(key = "header") {
            CourseProgressHeader(course = detail.course)
        }
        itemsIndexed(items = detail.lessons, key = { _, lesson -> lesson.id }) { index, lesson ->
            LessonRow(
                position = index + 1,
                lesson = lesson,
                onMarkCompleteClick = { onLessonCompleteClick(lesson.id) },
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun CourseProgressHeader(course: Course, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = course.instructor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CourseProgressBar(progressPercent = course.progressPercent)
        Text(
            text = stringResource(
                R.string.lessons_completed_summary,
                course.completedLessons,
                course.totalLessons,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.lessons_header),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailPreview() {
    val lessons = listOf(
        Lesson(101, 1, "Introduction", isCompleted = true),
        Lesson(102, 1, "Variables & Data Types", isCompleted = true),
        Lesson(103, 1, "Functions", isCompleted = false),
        Lesson(104, 1, "OOP", isCompleted = false),
    )
    LearningDashboardTheme {
        CourseDetailContent(
            uiState = CourseDetailUiState.Success(
                CourseDetail(
                    course = Course(1, "Python Programming", "John Smith", totalLessons = 4, completedLessons = 2),
                    lessons = lessons,
                )
            ),
            onBackClick = {},
            onLessonCompleteClick = {},
        )
    }
}