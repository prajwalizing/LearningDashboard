package com.prajwalhs.learningdashboard.fake

import com.prajwalhs.learningdashboard.data.remote.CourseApiService
import com.prajwalhs.learningdashboard.data.remote.dto.CourseDto

/** Returns [courses], or throws [error] when set (e.g. IOException to simulate offline). */
class FakeCourseApiService : CourseApiService {

    var courses: List<CourseDto> = emptyList()
    var error: Throwable? = null

    override suspend fun getCourses(): List<CourseDto> {
        error?.let { throw it }
        return courses
    }
}