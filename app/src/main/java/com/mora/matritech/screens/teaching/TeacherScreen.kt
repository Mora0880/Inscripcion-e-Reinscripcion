package com.mora.matritech.screens.teaching

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mora.matritech.data.local.SessionManager
import com.mora.matritech.ui.theme.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScreen(
    teacherViewModel: TeacherViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Profesor") },
                actions = {
                    IconButton(
                        onClick = {
                            sessionManager.logout()
                            navController.navigate(NavRoutes.Login.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar Sesión"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bienvenido Profesor",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    sessionManager.logout()
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }
    }
}