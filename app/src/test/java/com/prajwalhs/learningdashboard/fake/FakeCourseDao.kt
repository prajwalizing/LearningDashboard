package com.prajwalhs.learningdashboard.fake

import com.prajwalhs.learningdashboard.data.local.dao.CourseDao
import com.prajwalhs.learningdashboard.data.local.entity.CourseEntity
import com.prajwalhs.learningdashboard.data.local.entity.LessonEntity
import com.prajwalhs.learningdashboard.data.local.projection.CourseProgressProjection
import com.prajwalhs.learningdashboard.data.local.relation.CourseWithLessons
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

/**
 * In-memory CourseDao that behaves like Room: reads are reactive Flows and deleting a
 * course cascades to its lessons.
 *
 * syncWithRemote() is NOT overridden: the production default implementation (including
 * the local-completion merge rule) runs on top of this fake.
 */
class FakeCourseDao : CourseDao {

    private val courses = MutableStateFlow<Map<Int, CourseEntity>>(emptyMap())
    private val lessons = MutableStateFlow<Map<Int, LessonEntity>>(emptyMap())

    override fun observeCourseProgress(): Flow<List<CourseProgressProjection>> =
        combine(courses, lessons) { courseMap, lessonMap ->
            courseMap.values.sortedBy { it.id }.map { course ->
                val courseLessons = lessonMap.values.filter { it.courseId == course.id }
                CourseProgressProjection(
                    id = course.id,
                    title = course.title,
                    instructor = course.instructor,
                    totalLessons = courseLessons.size,
                    completedLessons = courseLessons.count { it.isCompleted },
                )
            }
        }

    override fun observeCourseWithLessons(courseId: Int): Flow<CourseWithLessons?> =
        combine(courses, lessons) { courseMap, lessonMap ->
            courseMap[courseId]?.let { course ->
                CourseWithLessons(
                    course = course,
                    lessons = lessonMap.values.filter { it.courseId == courseId },
                )
            }
        }

    override suspend fun getCompletedLessonIds(): List<Int> =
        lessons.value.values.filter { it.isCompleted }.map { it.id }

    override suspend fun upsertCourses(courses: List<CourseEntity>) {
        this.courses.update { current -> current + courses.associateBy { it.id } }
    }

    override suspend fun upsertLessons(lessons: List<LessonEntity>) {
        this.lessons.update { current -> current + lessons.associateBy { it.id } }
    }

    override suspend fun deleteCoursesNotIn(keepIds: List<Int>) {
        courses.update { current -> current.filterKeys { it in keepIds } }
        // Emulates ON DELETE CASCADE.
        lessons.update { current -> current.filterValues { it.courseId in keepIds } }
    }

    override suspend fun deleteLessonsNotIn(keepIds: List<Int>) {
        lessons.update { current -> current.filterKeys { it in keepIds } }
    }

    override suspend fun markLessonCompleted(lessonId: Int) {
        lessons.update { current ->
            val lesson = current[lessonId] ?: return@update current
            current + (lessonId to lesson.copy(isCompleted = true))
        }
    }
}