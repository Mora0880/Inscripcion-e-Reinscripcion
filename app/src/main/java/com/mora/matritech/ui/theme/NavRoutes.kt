package com.mora.matritech.ui.theme

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")

    object Register : NavRoutes("register")
    object Login : NavRoutes("login")
    object Home : NavRoutes("home")
    object Admin : NavRoutes("admin")
    object SuperAdmin : NavRoutes("superadmin")
    object Coordinator : NavRoutes("coordinator")
    object Student : NavRoutes("student")
    object Teacher : NavRoutes("teacher")
    object Representante : NavRoutes("representante")
}
