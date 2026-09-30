package com.prajwalhs.learningdashboard.domain.usecase

import com.prajwalhs.learningdashboard.domain.repository.CourseRepository
import javax.inject.Inject

class MarkLessonCompletedUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
) {
    suspend operator fun invoke(lessonId: Int) = courseRepository.markLessonCompleted(lessonId)
}