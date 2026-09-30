package com.prajwalhs.learningdashboard.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.prajwalhs.learningdashboard.data.local.entity.CourseEntity
import com.prajwalhs.learningdashboard.data.local.entity.LessonEntity

data class CourseWithLessons(
    @Embedded val course: CourseEntity,
    @Relation(parentColumn = "id", entityColumn = "course_id")
    val lessons: List<LessonEntity>,
)