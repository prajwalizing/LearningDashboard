package com.prajwalhs.learningdashboard.presentation.navigation


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.prajwalhs.learningdashboard.presentation.login.LoginScreen

@Composable
fun AppNavGraph(
    startDestination: AppRoute,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<AppRoute.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoute.Dashboard) {
                        // Remove Login from the back stack: Back from Dashboard exits the app.
                        popUpTo<AppRoute.Login> { inclusive = true }
                    }
                },
            )
        }

        composable<AppRoute.Dashboard> {
            // Temporary placeholder, replaced by DashboardScreen in the next commit.
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Dashboard")
            }
        }
    }
}