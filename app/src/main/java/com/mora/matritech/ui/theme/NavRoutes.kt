
package com.mora.matritech.ui

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("Splash")

    object register : NavRoutes("Register")
    object Login : NavRoutes("Login")
    object Home : NavRoutes("Home")
    object SuperAdmin : NavRoutes("superadmin")
    object Admin : NavRoutes("admin")
    object Coordinator : NavRoutes("coordinator")
    object Student : NavRoutes("student")
    object Teacher : NavRoutes("teacher")
    object Representante : NavRoutes("representante")
}
