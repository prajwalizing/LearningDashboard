package com.prajwalhs.learningdashboard.data.local.projection

import androidx.room.ColumnInfo

/**
 * Result of the dashboard query. Lesson counts are aggregated in SQL
 * so the dashboard never loads full lesson lists.
 */
data class CourseProgressProjection(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "instructor") val instructor: String,
    @ColumnInfo(name = "total_lessons") val totalLessons: Int,
    @ColumnInfo(name = "completed_lessons") val completedLessons: Int,
)