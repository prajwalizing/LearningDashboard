package com.prajwalhs.learningdashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalhs.learningdashboard.presentation.components.LoadingView
import com.prajwalhs.learningdashboard.presentation.navigation.AppNavGraph
import com.prajwalhs.learningdashboard.presentation.navigation.StartDestinationViewModel
import com.prajwalhs.learningdashboard.presentation.theme.LearningDashboardTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-Activity host. All screens are Compose destinations inside [AppNavGraph].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val startDestinationViewModel: StartDestinationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearningDashboardTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val startDestination by startDestinationViewModel.startDestination
                        .collectAsStateWithLifecycle()

                    when (val destination = startDestination) {
                        null -> LoadingView() // reading the session; avoids flashing Login
                        else -> AppNavGraph(startDestination = destination)
                    }
                }
            }
        }
    }
}