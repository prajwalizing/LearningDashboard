package com.prajwalhs.learningdashboard.domain.repository

import com.prajwalhs.learningdashboard.domain.model.AppResult
import com.prajwalhs.learningdashboard.domain.model.CourseDetail
import com.prajwalhs.learningdashboard.domain.model.Course
import kotlinx.coroutines.flow.Flow

/**
 * Local database is the single source of truth:
 * - observe* functions always read from local storage (works offline).
 * - [refreshCourses] fetches from the network and writes into local storage.
 */
interface CourseRepository {

    fun observeCourses(): Flow<List<Course>>

    /** Emits null if the course does not exist locally. */
    fun observeCourseDetail(courseId: Int): Flow<CourseDetail?>

    /** Fetches the latest courses from the API and stores them locally. */
    suspend fun refreshCourses(): AppResult<Unit>

    /** Marks a lesson as completed locally. Progress updates reactively through observers. */
    suspend fun markLessonCompleted(lessonId: Int)
}