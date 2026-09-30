package com.prajwalhs.learningdashboard.presentation.coursedetail

import com.prajwalhs.learningdashboard.domain.model.CourseDetail

sealed interface CourseDetailUiState {

    data object Loading : CourseDetailUiState

    data class Success(val detail: CourseDetail) : CourseDetailUiState

    /** Course missing locally, e.g. removed by a refresh while this screen was open. */
    data object NotFound : CourseDetailUiState
}