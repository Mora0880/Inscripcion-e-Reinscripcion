package com.mora.matritech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mora.matritech.data.local.SessionManager
import com.mora.matritech.data.repository.AuthRepository
import com.mora.matritech.data.repository.UserRepository
import com.mora.matritech.data.repository.InstitucionRepository
import com.mora.matritech.data.remote.supabase
import com.mora.matritech.screens.admin.AdminScreen
import com.mora.matritech.screens.admin.users.UserFormScreen
import com.mora.matritech.screens.admin.users.UserManagementScreen
import com.mora.matritech.screens.admin.users.UserViewModel
import com.mora.matritech.screens.coordinator.CoordinatorScreen
import com.mora.matritech.screens.coordinator.CoordinatorViewModel
import com.mora.matritech.screens.representante.RepresentanteScreen
import com.mora.matritech.screens.student.StudentScreen
import com.mora.matritech.screens.superadmin.SuperAdminScreen
import com.mora.matritech.screens.teaching.TeacherScreen
import com.mora.matritech.screens.teaching.TeacherViewModel
import com.mora.matritech.ui.screens.superadmin.AdminScreenCR
import com.mora.matritech.ui.screens.superadmin.AdminViewModelCR
import com.mora.matritech.ui.theme.MatriTechTheme
import com.mora.matritech.ui.theme.NavRoutes
import com.mora.matritech.ui.theme.login.LoginScreen
import com.mora.matritech.ui.theme.login.LoginViewModel
import com.mora.matritech.ui.theme.login.LoginViewModelFactory
import com.mora.matritech.ui.theme.register.RegisterScreen
import com.mora.matritech.ui.theme.splash.SplashScreen
import com.mora.matritech.screens.admin.AdminEnrollmentsScreen


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
    val sessionManager = remember { SessionManager(context) }

    // Determinar destino inicial
    val startDestination by produceState(initialValue = NavRoutes.Splash.route) {
        val role = sessionManager.getUserRole()
        value = when (role) {
            "admin" -> NavRoutes.Admin.route
            "superadmin" -> NavRoutes.SuperAdmin.route
            "representante" -> NavRoutes.Representante.route
            "estudiante" -> NavRoutes.Student.route
            "docente" -> NavRoutes.Teacher.route
            "coordinador" -> NavRoutes.Coordinator.route
            else -> NavRoutes.Splash.route
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        // ==================== AUTENTICACIÓN ====================
        composable(NavRoutes.Splash.route) {
            SplashScreen(navController)
        }

        composable(NavRoutes.Login.route) {
            val viewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(AuthRepository(), sessionManager)
            )
            LoginScreen(navController, viewModel)
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(navController)
        }

        // ==================== ROLES PRINCIPALES ====================
        composable(NavRoutes.Admin.route) {
            AdminScreen(navController)
        }

        composable(NavRoutes.SuperAdmin.route) {
            SuperAdminScreen(navController)
        }

        composable(NavRoutes.Representante.route) {
            RepresentanteScreen(navController)
        }

        composable(NavRoutes.Student.route) {
            StudentScreen(navController)
        }

        composable(NavRoutes.Teacher.route) {
            val vm: TeacherViewModel = viewModel()
            TeacherScreen(vm, navController)
        }

        composable(NavRoutes.Coordinator.route) {
            val vm: CoordinatorViewModel = viewModel()
            CoordinatorScreen(vm, navController)
        }

        composable("admin/enrollments") {
            AdminEnrollmentsScreen(navController = navController)
        }

        composable(
            route = "admin/enrollments/detail/{enrollmentId}",
            arguments = listOf(navArgument("enrollmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val enrollmentId = backStackEntry.arguments?.getString("enrollmentId")
            // EnrollmentDetailScreen(navController, enrollmentId)  // Por implementar si quieres
        }

        // ==================== CRUD DE USUARIOS (ADMIN) ====================

        // Lista de usuarios
        composable(NavRoutes.UserManagement.route) {
            val viewModel: UserViewModel = viewModel()
            val context = LocalContext.current
            val sessionManager = remember { SessionManager(context) }

            // Obtener el rol del usuario actual
            val currentUserRole = sessionManager.getUserRole()
            val currentUserRoleId = when (currentUserRole) {
                "superadmin" -> 0
                "admin" -> 1
                "coordinador" -> 2
                "estudiante" -> 3
                "docente" -> 4
                "representante" -> 5
                else -> null
            }

            UserManagementScreen(
                viewModel = viewModel,
                currentUserRoleId = currentUserRoleId,
                onNavigateToForm = { userId ->
                    if (userId != null) {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("userId", userId)
                        navController.navigate(NavRoutes.UserEdit.createRoute(userId))
                    } else {
                        viewModel.prepareCreateUser()
                        navController.navigate(NavRoutes.UserForm.route)
                    }
                }
            )
        }

        // Formulario para crear usuario
        composable(NavRoutes.UserForm.route) {
            val viewModel: UserViewModel = viewModel()
            UserFormScreen(
                userId = null, // null = crear nuevo
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Formulario para editar usuario (con ID)
        composable(
            route = NavRoutes.UserEdit.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            val viewModel: UserViewModel = viewModel()

            UserFormScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ==================== CRUD DE ADMINISTRADORES (SUPERADMIN) ====================

        // Gestión de Administradores
        composable(NavRoutes.AdminManagement.route) {
            // Crear instancias de los repositorios
            val userRepository = remember { UserRepository() }
            val institucionRepository = remember { InstitucionRepository(supabase) }

            // Crear el ViewModel manualmente
            val viewModel = remember {
                AdminViewModelCR(
                    userRepository = userRepository,
                    institucionRepository = institucionRepository
                )
            }

            AdminScreenCR(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // ==================== OTRAS SECCIONES DE ADMIN (FUTURAS) ====================

        // Dashboard del admin
        composable(NavRoutes.AdminDashboard.route) {
            AdminScreen(navController)
        }

        // Reportes del admin
        composable(NavRoutes.AdminReports.route) {
            AdminScreen(navController)
        }

        // Configuración del admin
        composable(NavRoutes.AdminSettings.route) {
            AdminScreen(navController)
        }
    }
}