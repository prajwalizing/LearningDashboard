package com.prajwalhs.learningdashboard.di

import com.prajwalhs.learningdashboard.data.repository.AuthRepositoryImpl
import com.prajwalhs.learningdashboard.data.repository.CourseRepositoryImpl
import com.prajwalhs.learningdashboard.domain.repository.AuthRepository
import com.prajwalhs.learningdashboard.domain.repository.CourseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds domain interfaces to data implementations.
 * The domain and presentation layers only ever see the interfaces.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCourseRepository(impl: CourseRepositoryImpl): CourseRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}