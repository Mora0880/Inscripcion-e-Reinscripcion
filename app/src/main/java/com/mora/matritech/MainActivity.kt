package com.mora.matritech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mora.matritech.ui.NavRoutes
import com.mora.matritech.ui.home.HomeScreen
import com.mora.matritech.ui.login.LoginScreen
import com.mora.matritech.ui.theme.MatriTechTheme
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import com.mora.matritech.ui.Splash.SplashScreen
import com.mora.matritech.ui.theme.Register.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MatriTechTheme {
                AppNavigation()
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
            RegisterScreen(
                onRegisterClick = { email, password, name ->
                    // Aquí luego llamarás Supabase para crear el usuario
                },
                onLoginClick = {
                    navController.navigate("login") {
                        popUpTo(NavRoutes.register.route) { inclusive = true }
                    }
                }
            )
        }


        // Login Screen
        composable(NavRoutes.Login.route) {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen
        composable(NavRoutes.Home.route) {
            HomeScreen()
        }
    }
}

