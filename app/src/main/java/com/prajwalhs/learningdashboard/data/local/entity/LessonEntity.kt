package com.prajwalhs.learningdashboard.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["course_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["course_id"])],
)
data class LessonEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "course_id") val courseId: Int,
    // Preserves the server's lesson order; @Relation does not guarantee ordering.
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
)