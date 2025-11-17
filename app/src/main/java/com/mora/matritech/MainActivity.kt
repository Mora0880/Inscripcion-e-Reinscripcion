package com.mora.matritech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mora.matritech.ui.NavRoutes
import com.mora.matritech.ui.home.HomeScreen
import com.mora.matritech.ui.login.LoginScreen
import com.mora.matritech.ui.theme.MatriTechTheme
import androidx.compose.runtime.Composable
import com.mora.matritech.data.remote.supabase
import com.mora.matritech.ui.Splash.SplashScreen
import com.mora.matritech.ui.theme.register.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MatriTechTheme {
                AppNavigation()
                val client = supabase
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route
    ) {
        // Splash Screen
        composable(NavRoutes.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.register.route) {
            RegisterScreen(navController = navController)
        }


        // Login Screen
        composable(NavRoutes.Login.route) {
            LoginScreen(
                navController = navController
            )
        }

        // Home Screen
        composable(NavRoutes.Home.route) {
            HomeScreen()
        }
    }
}

