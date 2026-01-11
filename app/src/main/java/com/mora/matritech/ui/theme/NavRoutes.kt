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

    object AdminEnrollments : NavRoutes("admin/enrollments")
    object SuperAdmin : NavRoutes("superadmin")
    object Coordinator : NavRoutes("coordinator")
    object Representante : NavRoutes("representante")

    // CRUD de Usuarios (Admin) - YA EXISTENTE
    object UserManagement : NavRoutes("admin/users")
    object UserForm : NavRoutes("admin/users/form")
    object UserEdit : NavRoutes("admin/users/edit/{userId}") {
        fun createRoute(userId: String) = "admin/users/edit/$userId"
    }

    // CRUD de Administradores (SuperAdmin) - NUEVO
    object AdminManagement : NavRoutes("superadmin/admins")

    // Otras secciones de Admin
    object AdminDashboard : NavRoutes("admin/dashboard")
    object AdminReports : NavRoutes("admin/reports")
    object AdminSettings : NavRoutes("admin/settings")
}