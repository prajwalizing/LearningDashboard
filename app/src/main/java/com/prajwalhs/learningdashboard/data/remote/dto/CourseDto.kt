package com.prajwalhs.learningdashboard.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network representation of a course. Kept separate from the domain model so API changes
 * are absorbed by the mapper instead of rippling through the app.
 */
@Serializable
data class CourseDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("instructor") val instructor: String,
    @SerialName("lessons") val lessons: List<LessonDto> = emptyList(),
)