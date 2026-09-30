package com.prajwalhs.learningdashboard.domain.util

import kotlin.math.roundToInt

object ProgressCalculator {

    /**
     * Returns course progress as a whole percentage (0..100), rounded half-up.
     * Example: 6 of 16 lessons = 37.5% -> 38%.
     *
     * Defensive against bad data: returns 0 when [total] is 0 or negative,
     * and clamps [completed] into 0..[total].
     */
    fun percent(completed: Int, total: Int): Int {
        if (total <= 0) return 0
        val safeCompleted = completed.coerceIn(0, total)
        return (safeCompleted * 100.0 / total).roundToInt()
    }
}