package com.prajwalhs.learningdashboard.data.mapper

import com.prajwalhs.learningdashboard.data.local.entity.CourseEntity
import com.prajwalhs.learningdashboard.data.local.entity.LessonEntity
import com.prajwalhs.learningdashboard.data.local.projection.CourseProgressProjection
import com.prajwalhs.learningdashboard.data.local.relation.CourseWithLessons
import com.prajwalhs.learningdashboard.data.remote.dto.CourseDto
import com.prajwalhs.learningdashboard.domain.model.Course
import com.prajwalhs.learningdashboard.domain.model.CourseDetail
import com.prajwalhs.learningdashboard.domain.model.Lesson

// ---------- Network -> Database ----------

fun CourseDto.toEntity(): CourseEntity =
    CourseEntity(id = id, title = title, instructor = instructor)

/** The lesson's position in the API response becomes its persisted display order. */
fun CourseDto.toLessonEntities(): List<LessonEntity> =
    lessons.mapIndexed { index, lesson ->
        LessonEntity(
            id = lesson.id,
            courseId = id,
            orderIndex = index,
            title = lesson.title,
            isCompleted = lesson.isCompleted,
        )
    }

// ---------- Database -> Domain ----------

fun CourseProgressProjection.toDomain(): Course =
    Course(
        id = id,
        title = title,
        instructor = instructor,
        totalLessons = totalLessons,
        completedLessons = completedLessons,
    )

fun LessonEntity.toDomain(): Lesson =
    Lesson(id = id, courseId = courseId, title = title, isCompleted = isCompleted)

fun CourseWithLessons.toDomain(): CourseDetail {
    val orderedLessons = lessons.sortedBy { it.orderIndex }
    return CourseDetail(
        course = Course(
            id = course.id,
            title = course.title,
            instructor = course.instructor,
            totalLessons = orderedLessons.size,
            completedLessons = orderedLessons.count { it.isCompleted },
        ),
        lessons = orderedLessons.map { it.toDomain() },
    )
}
