package com.prajwalhs.learningdashboard.domain.usecase

import com.prajwalhs.learningdashboard.domain.model.Course
import com.prajwalhs.learningdashboard.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCoursesUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
) {
    operator fun invoke(): Flow<List<Course>> = courseRepository.observeCourses()
}