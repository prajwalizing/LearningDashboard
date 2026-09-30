package com.prajwalhs.learningdashboard.di

import android.content.Context
import androidx.room.Room
import com.prajwalhs.learningdashboard.data.local.LearningDatabase
import com.prajwalhs.learningdashboard.data.local.dao.CourseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLearningDatabase(@ApplicationContext context: Context): LearningDatabase =
        Room.databaseBuilder(
            context,
            LearningDatabase::class.java,
            LearningDatabase.DATABASE_NAME,
        ).build()
    // No fallbackToDestructiveMigration(): it would silently wipe local progress.
    // Schema changes should ship with explicit migrations (schemas are exported for this).

    @Provides
    fun provideCourseDao(database: LearningDatabase): CourseDao = database.courseDao()
}