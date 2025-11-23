package com.mora.matritech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.*
import com.mora.matritech.ui.NavRoutes
import com.mora.matritech.ui.login.LoginScreen
import com.mora.matritech.ui.theme.MatriTechTheme
import com.mora.matritech.ui.Splash.SplashScreen
import com.mora.matritech.ui.theme.register.RegisterScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import com.mora.matritech.screens.admin.AdminScreen
import com.mora.matritech.screens.coordinator.CoordinatorScreen
import com.mora.matritech.screens.representante.RepresentanteScreen
import com.mora.matritech.screens.student.StudentScreen
import com.mora.matritech.screens.teaching.TeacherScreen
import com.mora.matritech.ui.login.LoginViewModel

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
    val loginViewModel: LoginViewModel = viewModel()

    val loginState by loginViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route
    ) {
        composable(NavRoutes.Splash.route) {
            SplashScreen(navController)
        }

        composable(NavRoutes.register.route) {
            RegisterScreen(navController)
        }

        composable(NavRoutes.Login.route) {
            LoginScreen(navController, loginViewModel)
        }

        composable(NavRoutes.Admin.route) { AdminScreen() }
        composable(NavRoutes.Coordinator.route) { CoordinatorScreen() }
        composable(NavRoutes.Student.route) { StudentScreen() }
        composable(NavRoutes.Teacher.route) { TeacherScreen() }
        composable(NavRoutes.Representante.route) { RepresentanteScreen() }
    }
}
