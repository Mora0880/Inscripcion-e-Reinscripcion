package com.mora.matritech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mora.matritech.data.local.SessionManager
import com.mora.matritech.data.repository.AuthRepository
import com.mora.matritech.screens.admin.AdminScreen
import com.mora.matritech.screens.coordinator.CoordinatorScreen
import com.mora.matritech.screens.coordinator.CoordinatorViewModel
import com.mora.matritech.screens.representante.RepresentanteScreen
import com.mora.matritech.screens.student.StudentScreen
import com.mora.matritech.screens.superadmin.SuperAdminScreen
import com.mora.matritech.screens.teaching.TeacherScreen
import com.mora.matritech.screens.teaching.TeacherViewModel
import com.mora.matritech.ui.theme.NavRoutes
import com.mora.matritech.ui.theme.MatriTechTheme
import com.mora.matritech.ui.theme.login.LoginScreen
import com.mora.matritech.ui.theme.login.LoginViewModel
import com.mora.matritech.ui.theme.login.LoginViewModelFactory
import com.mora.matritech.ui.theme.register.RegisterScreen
import com.mora.matritech.ui.theme.splash.SplashScreen

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
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route
    ) {
        composable(NavRoutes.Splash.route) {
            SplashScreen(navController)
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(navController)
        }

        composable(NavRoutes.Login.route) {
            val authRepository = AuthRepository()
            val sessionManager = SessionManager(context)

            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(authRepository, sessionManager)
            )

            LoginScreen(navController, loginViewModel)
        }

        composable(NavRoutes.Admin.route) {
            AdminScreen(navController)  // ✅ Agregado navController
        }

        composable(NavRoutes.Coordinator.route) {
            val coordinatorViewModel: CoordinatorViewModel = viewModel()
            CoordinatorScreen(coordinatorViewModel, navController)  // ✅ Agregado navController
        }

        composable(NavRoutes.Student.route) {
            StudentScreen(navController)  // ✅ Agregado navController
        }

        composable(NavRoutes.Teacher.route) {
            val teacherViewModel: TeacherViewModel = viewModel()
            TeacherScreen(teacherViewModel, navController)  // ✅ Agregado navController - CORREGIDO
        }

        composable(NavRoutes.Representante.route) {
            RepresentanteScreen(navController)  // ✅ Agregado navController
        }

        composable(NavRoutes.SuperAdmin.route) { SuperAdminScreen() }
    }
}