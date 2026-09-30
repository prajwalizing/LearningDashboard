package com.prajwalhs.learningdashboard.domain.model

import com.prajwalhs.learningdashboard.domain.util.ProgressCalculator

/**
 * A course as shown on the dashboard.
 * Progress is derived from lesson counts, never stored, so it cannot drift out of sync.
 */
data class Course(
    val id: Int,
    val title: String,
    val instructor: String,
    val totalLessons: Int,
    val completedLessons: Int,
) {
    val progressPercent: Int
        get() = ProgressCalculator.percent(completed = completedLessons, total = totalLessons)
}