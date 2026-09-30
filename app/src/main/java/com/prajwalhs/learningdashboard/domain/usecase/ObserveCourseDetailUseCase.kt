package com.prajwalhs.learningdashboard.domain.usecase

import com.prajwalhs.learningdashboard.domain.model.CourseDetail
import com.prajwalhs.learningdashboard.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCourseDetailUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
) {
    operator fun invoke(courseId: Int): Flow<CourseDetail?> =
        courseRepository.observeCourseDetail(courseId)
}