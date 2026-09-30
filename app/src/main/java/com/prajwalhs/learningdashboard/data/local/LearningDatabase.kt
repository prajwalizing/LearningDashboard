package com.prajwalhs.learningdashboard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.prajwalhs.learningdashboard.data.local.dao.CourseDao
import com.prajwalhs.learningdashboard.data.local.entity.CourseEntity
import com.prajwalhs.learningdashboard.data.local.entity.LessonEntity

@Database(
    entities = [CourseEntity::class, LessonEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class LearningDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao

    companion object {
        const val DATABASE_NAME = "learning_dashboard.db"
    }
}