package com.prajwalhs.learningdashboard.data.repository

import com.prajwalhs.learningdashboard.data.local.entity.CourseEntity
import com.prajwalhs.learningdashboard.data.local.entity.LessonEntity
import com.prajwalhs.learningdashboard.data.remote.dto.CourseDto
import com.prajwalhs.learningdashboard.data.remote.dto.LessonDto
import com.prajwalhs.learningdashboard.domain.model.AppError
import com.prajwalhs.learningdashboard.domain.model.AppResult
import com.prajwalhs.learningdashboard.fake.FakeCourseApiService
import com.prajwalhs.learningdashboard.fake.FakeCourseDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class CourseRepositoryImplTest {

    private val api = FakeCourseApiService()
    private val dao = FakeCourseDao()
    private val repository = CourseRepositoryImpl(courseApi = api, courseDao = dao)

    @Test
    fun `when offline, refresh fails but cached courses are still available`() = runTest {
        // Given: courses were loaded earlier and cached
        dao.upsertCourses(listOf(CourseEntity(id = 1, title = "Python Programming", instructor = "John Smith")))
        dao.upsertLessons(
            listOf(
                lessonEntity(id = 101, courseId = 1, order = 0, isCompleted = true),
                lessonEntity(id = 102, courseId = 1, order = 1, isCompleted = false),
            )
        )
        // And: the device is offline
        api.error = IOException("Unable to resolve host")

        // When
        val result = repository.refreshCourses()

        // Then: the failure is reported as a network error...
        assertEquals(AppResult.Failure(AppError.Network), result)
        // ...and the cached data is untouched and still observable
        val courses = repository.observeCourses().first()
        assertEquals(1, courses.size)
        assertEquals("Python Programming", courses.single().title)
        assertEquals(50, courses.single().progressPercent)
    }

    @Test
    fun `refresh keeps lessons completed locally even when server still reports them pending`() = runTest {
        // Given: lesson 101 was completed on this device, but the server has not been told yet
        dao.upsertCourses(listOf(CourseEntity(id = 1, title = "Python Programming", instructor = "John Smith")))
        dao.upsertLessons(listOf(lessonEntity(id = 101, courseId = 1, order = 0, isCompleted = true)))
        api.courses = listOf(
            courseDto(
                id = 1,
                lessons = listOf(
                    LessonDto(id = 101, title = "Introduction", isCompleted = false),
                    LessonDto(id = 102, title = "Variables", isCompleted = false),
                ),
            )
        )

        // When
        val result = repository.refreshCourses()

        // Then: local completion wins; the new lesson from the server is added
        assertEquals(AppResult.Success(Unit), result)
        val detail = repository.observeCourseDetail(1).first()!!
        assertEquals(listOf(101, 102), detail.lessons.map { it.id })
        assertTrue(detail.lessons.first { it.id == 101 }.isCompleted)
        assertEquals(50, detail.course.progressPercent)
    }

    @Test
    fun `refresh removes courses that no longer exist on the server`() = runTest {
        dao.upsertCourses(listOf(CourseEntity(id = 99, title = "Retired Course", instructor = "Someone")))
        dao.upsertLessons(listOf(lessonEntity(id = 9901, courseId = 99, order = 0, isCompleted = false)))
        api.courses = listOf(courseDto(id = 1, lessons = listOf(LessonDto(101, "Introduction"))))

        repository.refreshCourses()

        assertEquals(listOf(1), repository.observeCourses().first().map { it.id })
    }

    @Test
    fun `server error is mapped to AppError Server with the status code`() = runTest {
        api.error = HttpException(Response.error<Any>(500, "".toResponseBody()))

        val result = repository.refreshCourses()

        assertEquals(AppResult.Failure(AppError.Server(500)), result)
    }

    // ---------- Test data builders ----------

    private fun lessonEntity(id: Int, courseId: Int, order: Int, isCompleted: Boolean) =
        LessonEntity(
            id = id,
            courseId = courseId,
            orderIndex = order,
            title = "Lesson $id",
            isCompleted = isCompleted,
        )

    private fun courseDto(id: Int, lessons: List<LessonDto>) =
        CourseDto(id = id, title = "Course $id", instructor = "Instructor", lessons = lessons)
}