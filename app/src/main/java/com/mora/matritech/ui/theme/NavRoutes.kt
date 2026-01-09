package com.mora.matritech.ui.theme

sealed class NavRoutes(val route: String) {
    // Autenticación
    object Splash : NavRoutes("splash")
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")

    // Roles principales
    object Student : NavRoutes("student")
    object Teacher : NavRoutes("teaching")
    object Admin : NavRoutes("admin")
    object SuperAdmin : NavRoutes("superadmin")
    object Coordinator : NavRoutes("coordinator")
    object Representante : NavRoutes("representante")

    // CRUD de Usuarios (Admin)
    object UserManagement : NavRoutes("admin/users")
    object UserForm : NavRoutes("admin/users/form")
    object UserEdit : NavRoutes("admin/users/edit/{userId}") {
        fun createRoute(userId: String) = "admin/users/edit/$userId"
    }

    // Otras secciones de Admin
    object AdminDashboard : NavRoutes("admin/dashboard")
    object AdminReports : NavRoutes("admin/reports")
    object AdminSettings : NavRoutes("admin/settings")
}