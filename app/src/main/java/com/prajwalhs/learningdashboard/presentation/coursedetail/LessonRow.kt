package com.prajwalhs.learningdashboard.presentation.coursedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prajwalhs.learningdashboard.R
import com.prajwalhs.learningdashboard.domain.model.Lesson
import com.prajwalhs.learningdashboard.presentation.theme.LearningDashboardTheme

@Composable
fun LessonRow(
    position: Int,
    lesson: Lesson,
    onMarkCompleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$position. ${lesson.title}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(
                    if (lesson.isCompleted) R.string.lesson_status_completed
                    else R.string.lesson_status_pending
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (lesson.isCompleted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (!lesson.isCompleted) {
            OutlinedButton(onClick = onMarkCompleteClick) {
                Text(text = stringResource(R.string.action_mark_complete))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LessonRowPreview() {
    LearningDashboardTheme {
        Column {
            LessonRow(1, Lesson(101, 1, "Introduction", isCompleted = true), onMarkCompleteClick = {})
            LessonRow(2, Lesson(102, 1, "Functions", isCompleted = false), onMarkCompleteClick = {})
        }
    }
}