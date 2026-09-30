package com.prajwalhs.learningdashboard

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Triggers Hilt code generation and hosts the app-level DI container.
 */
@HiltAndroidApp
class LearningDashboardApp : Application()