package com.mora.matritech.screens.student


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mora.matritech.data.local.SessionManager
import com.mora.matritech.ui.theme.MatriTechTheme
import com.mora.matritech.ui.theme.NavRoutes

// -----------------------------------------------------------------
// BOTTOM BAR
// -----------------------------------------------------------------
@Composable
private fun StudentBottomBar() {
    BottomAppBar(containerColor = Color.White, modifier = Modifier.height(56.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomIcon("inicio", "inicio", {}, Icons.Filled.Home)
            BottomIcon("notas", "notas", {}, Icons.Filled.Grade)
            BottomIcon("horario", "horario", {}, Icons.Filled.Schedule)
        }
    }
}

@Composable
private fun BottomIcon(
    item: String,
    selected: String,
    onClick: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    IconButton(onClick = { onClick(item) }) {
        Icon(
            imageVector = icon,
            contentDescription = item,
            tint = if (selected == item) Color(0xFF00ACC1) else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
    }
}

// -----------------------------------------------------------------
// HEADER
// -----------------------------------------------------------------
@Composable
private fun StudentHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MatriTech", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Panel del Estudiante", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF00ACC1)) {
            Text(
                "ESTUDIANTE",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------
// ESTADÍSTICAS
// -----------------------------------------------------------------
@Composable
private fun StudentStatisticsSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("PROMEDIO GENERAL", "8.7", Icons.Filled.Star, Color(0xFF00ACC1))
            StatCard("ASISTENCIA", "94%", Icons.Filled.HowToReg, Color(0xFF26A69A))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("MATERIAS", "8", Icons.Filled.Book, Color(0xFF0097A7))
            StatCard("FALTAS", "3", Icons.Filled.Warning, Color(0xFFFF9800))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Estudiante") },
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
                text = "Bienvenido Estudiante",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StudentScreenPreview(navController: NavHostController = NavHostController(LocalContext.current)) {
    MatriTechTheme {
        StudentScreen(navController)
    }
}