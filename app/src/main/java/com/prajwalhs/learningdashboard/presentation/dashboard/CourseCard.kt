package com.prajwalhs.learningdashboard.presentation.dashboard


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prajwalhs.learningdashboard.R
import com.prajwalhs.learningdashboard.domain.model.Course
import com.prajwalhs.learningdashboard.presentation.components.CourseProgressBar
import com.prajwalhs.learningdashboard.presentation.theme.LearningDashboardTheme

@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = course.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = course.instructor,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = pluralStringResource(
                    R.plurals.lesson_count,
                    course.totalLessons,
                    course.totalLessons,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CourseProgressBar(progressPercent = course.progressPercent)
            Button(onClick = onClick, modifier = Modifier.align(Alignment.End)) {
                Text(text = stringResource(course.actionLabelRes()))
            }
        }
    }
}

private fun Course.actionLabelRes(): Int = when (progressPercent) {
    0 -> R.string.action_start
    100 -> R.string.action_review
    else -> R.string.action_continue
}

@Preview(showBackground = true)
@Composable
private fun CourseCardPreview() {
    LearningDashboardTheme {
        CourseCard(
            course = Course(
                id = 1,
                title = "Python Programming",
                instructor = "John Smith",
                totalLessons = 20,
                completedLessons = 13,
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}