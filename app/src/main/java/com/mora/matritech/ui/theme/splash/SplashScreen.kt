package com.mora.matritech.ui.theme.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mora.matritech.R
import com.mora.matritech.data.local.SessionManager
import com.mora.matritech.model.UserRole
import com.mora.matritech.ui.theme.NavRoutes
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    LaunchedEffect(Unit) {
        delay(2000) // Espera 2 segundos para mostrar el splash

        // ✅ VERIFICA SI HAY SESIÓN ACTIVA usando tu SessionManager
        if (sessionManager.isLoggedIn()) {
            val userRole = sessionManager.getUserRole()  // ✅ Usa getUserRole()

            val route = when (userRole) {
                UserRole.ADMIN.name -> NavRoutes.Admin.route
                UserRole.COORDINATOR.name -> NavRoutes.Coordinator.route
                UserRole.STUDENT.name -> NavRoutes.Student.route
                UserRole.TEACHER.name -> NavRoutes.Teacher.route
                UserRole.REPRESENTANTE.name -> NavRoutes.Representante.route
                else -> NavRoutes.Login.route
            }

            navController.navigate(route) {
                popUpTo(NavRoutes.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            // No hay sesión, ir al login
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(NavRoutes.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // UI del Splash
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "MatriTech Logo",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MatriTech",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            CircularProgressIndicator()
        }
    }
}


