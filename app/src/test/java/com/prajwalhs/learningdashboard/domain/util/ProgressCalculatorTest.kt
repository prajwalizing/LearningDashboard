package com.prajwalhs.learningdashboard.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {

    @Test
    fun `returns exact percentage when it divides evenly`() {
        assertEquals(65, ProgressCalculator.percent(completed = 13, total = 20))
    }

    @Test
    fun `rounds half up to a whole percentage`() {
        // 6 / 16 = 37.5% -> 38%
        assertEquals(38, ProgressCalculator.percent(completed = 6, total = 16))
    }

    @Test
    fun `returns 0 when course has no lessons instead of dividing by zero`() {
        assertEquals(0, ProgressCalculator.percent(completed = 0, total = 0))
    }

    @Test
    fun `returns 100 when all lessons are completed`() {
        assertEquals(100, ProgressCalculator.percent(completed = 28, total = 28))
    }

    @Test
    fun `clamps inconsistent data into 0 to 100`() {
        assertEquals(100, ProgressCalculator.percent(completed = 25, total = 20))
        assertEquals(0, ProgressCalculator.percent(completed = -3, total = 20))
    }
}