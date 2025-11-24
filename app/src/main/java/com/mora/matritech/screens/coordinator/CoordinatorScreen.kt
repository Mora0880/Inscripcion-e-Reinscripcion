package com.mora.matritech.screens.coordinator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mora.matritech.data.local.SessionManager
import com.mora.matritech.ui.theme.NavRoutes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoordinatorScreen(
    viewModel: CoordinatorViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val groups = viewModel.groups.collectAsState()

    // Cargar lista cuando entra a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel del Coordinador") },
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
                .padding(16.dp)
        ) {
            Text(
                text = "Grupos Asignados",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(groups.value) { group ->
                    GroupCard(group)
                }
            }
        }
    }
}

@Composable
fun GroupCard(group: Group) {
    Card(
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Grado: ${group.grade}",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Sección: ${group.section}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Estudiantes inscritos: ${group.enrolled}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
