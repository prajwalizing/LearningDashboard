package com.prajwalhs.learningdashboard.domain.model

/**
 * A course together with its ordered lessons, used by the Course Detail screen.
 */
data class CourseDetail(
    val course: Course,
    val lessons: List<Lesson>,
)