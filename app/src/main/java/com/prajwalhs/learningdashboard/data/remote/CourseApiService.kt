package com.prajwalhs.learningdashboard.data.remote

import com.prajwalhs.learningdashboard.data.remote.dto.CourseDto
import retrofit2.http.GET

interface CourseApiService {

    /** Returns all courses with their lessons. Throws IOException / HttpException on failure. */
    @GET("courses.json")
    suspend fun getCourses(): List<CourseDto>
}