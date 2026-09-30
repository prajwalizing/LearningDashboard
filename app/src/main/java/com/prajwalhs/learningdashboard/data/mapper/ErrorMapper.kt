package com.prajwalhs.learningdashboard.data.mapper

import com.prajwalhs.learningdashboard.domain.model.AppError
import retrofit2.HttpException
import java.io.IOException

/**
 * Translates low-level exceptions into domain errors so no layer above `data`
 * depends on Retrofit or java.io types.
 */
fun Throwable.toAppError(): AppError = when (this) {
    is IOException -> AppError.Network          // offline, timeout, DNS failure
    is HttpException -> AppError.Server(code())  // non-2xx response
    else -> AppError.Unknown                     // e.g. malformed JSON
}