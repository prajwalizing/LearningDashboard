package com.prajwalhs.learningdashboard.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations. Arguments (added later for CourseDetail)
 * are constructor properties, so there is no string parsing of routes.
 */
sealed interface AppRoute {

    @Serializable
    data object Login : AppRoute

    @Serializable
    data object Dashboard : AppRoute

    @Serializable
    data class CourseDetail(val courseId: Int) : AppRoute
}