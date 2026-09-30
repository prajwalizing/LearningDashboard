package com.prajwalhs.learningdashboard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prajwalhs.learningdashboard.R
import com.prajwalhs.learningdashboard.presentation.theme.LearningDashboardTheme

/**
 * Progress bar with a percentage label. Screen readers get a single
 * "65 percent complete" announcement instead of two separate elements.
 */
@Composable
fun CourseProgressBar(
    progressPercent: Int,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.progress_content_description, progressPercent)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LinearProgressIndicator(
            progress = { progressPercent.coerceIn(0, 100) / 100f },
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.progress_percent, progressPercent),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseProgressBarPreview() {
    LearningDashboardTheme { CourseProgressBar(progressPercent = 65) }
}