package com.prajwalhs.learningdashboard.data.repository

import com.prajwalhs.learningdashboard.data.local.dao.CourseDao
import com.prajwalhs.learningdashboard.data.mapper.toAppError
import com.prajwalhs.learningdashboard.data.mapper.toDomain
import com.prajwalhs.learningdashboard.data.mapper.toEntity
import com.prajwalhs.learningdashboard.data.mapper.toLessonEntities
import com.prajwalhs.learningdashboard.data.remote.CourseApiService
import com.prajwalhs.learningdashboard.domain.model.AppResult
import com.prajwalhs.learningdashboard.domain.model.Course
import com.prajwalhs.learningdashboard.domain.model.CourseDetail
import com.prajwalhs.learningdashboard.domain.repository.CourseRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Offline-first repository. Room is the single source of truth:
 * - Reads always come from Room, so previously loaded data is available offline.
 * - [refreshCourses] pulls from the API and writes into Room; observers update automatically.
 *
 * Retrofit and Room suspend/Flow APIs are main-safe, so no explicit dispatcher is needed here.
 */
class CourseRepositoryImpl @Inject constructor(
    private val courseApi: CourseApiService,
    private val courseDao: CourseDao,
) : CourseRepository {

    override fun observeCourses(): Flow<List<Course>> =
        courseDao.observeCourseProgress().map { rows -> rows.map { it.toDomain() } }

    override fun observeCourseDetail(courseId: Int): Flow<CourseDetail?> =
        courseDao.observeCourseWithLessons(courseId).map { it?.toDomain() }

    override suspend fun refreshCourses(): AppResult<Unit> {
        return try {
            val remoteCourses = courseApi.getCourses()
            courseDao.syncWithRemote(
                courses = remoteCourses.map { it.toEntity() },
                lessons = remoteCourses.flatMap { it.toLessonEntities() },
            )
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Cached data in Room is untouched; the caller decides how to present the failure.
            AppResult.Failure(e.toAppError())
        }
    }

    override suspend fun markLessonCompleted(lessonId: Int) {
        courseDao.markLessonCompleted(lessonId)
    }
}