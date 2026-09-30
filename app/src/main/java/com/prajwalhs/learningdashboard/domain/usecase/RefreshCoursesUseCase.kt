package com.prajwalhs.learningdashboard.domain.usecase

import com.prajwalhs.learningdashboard.domain.model.AppResult
import com.prajwalhs.learningdashboard.domain.repository.CourseRepository
import javax.inject.Inject

class RefreshCoursesUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = courseRepository.refreshCourses()
}