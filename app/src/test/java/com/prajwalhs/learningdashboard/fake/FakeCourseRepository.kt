package com.prajwalhs.learningdashboard.fake

import com.prajwalhs.learningdashboard.domain.model.AppResult
import com.prajwalhs.learningdashboard.domain.model.Course
import com.prajwalhs.learningdashboard.domain.model.CourseDetail
import com.prajwalhs.learningdashboard.domain.repository.CourseRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Controllable repository for ViewModel tests.
 * - [cachedCourses]: what "Room" currently holds.
 * - [refreshResult]: outcome of the next refresh.
 * - [coursesAfterRefresh]: written to the cache when a refresh succeeds.
 * - [refreshGate]: when set, refresh suspends until completed, to observe in-flight states.
 */
class FakeCourseRepository : CourseRepository {

    val cachedCourses = MutableStateFlow<List<Course>>(emptyList())
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    var coursesAfterRefresh: List<Course>? = null
    var refreshGate: CompletableDeferred<Unit>? = null

    override fun observeCourses(): Flow<List<Course>> = cachedCourses

    override fun observeCourseDetail(courseId: Int): Flow<CourseDetail?> = flowOf(null)

    override suspend fun refreshCourses(): AppResult<Unit> {
        refreshGate?.await()
        if (refreshResult is AppResult.Success) {
            coursesAfterRefresh?.let { cachedCourses.value = it }
        }
        return refreshResult
    }

    override suspend fun markLessonCompleted(lessonId: Int) = Unit
}