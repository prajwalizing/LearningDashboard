package com.prajwalhs.learningdashboard.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LessonDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("isCompleted") val isCompleted: Boolean = false,
)