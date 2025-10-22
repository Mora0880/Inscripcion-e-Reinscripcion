
package com.mora.matritech.ui

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("Splash")
    object Login : NavRoutes("Login")
    object Home : NavRoutes("Home")
}
