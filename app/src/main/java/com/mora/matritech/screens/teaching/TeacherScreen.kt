package com.mora.matritech.screens.teaching

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mora.matritech.data.local.SessionManager
import com.mora.matritech.ui.theme.NavRoutes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScreen(
    viewModel: TeacherViewModel = viewModel(),
    navController: NavHostController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            TeacherDrawerContent(
                onItemClick = { scope.launch { drawerState.close() } },
                onLogout = {
                    sessionManager.logout()
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        },
        content = {
            Scaffold(
                topBar = {
                    TeacherTopBar(onMenuClick = { scope.launch { drawerState.open() } })
                },
                bottomBar = {
                    TeacherBottomBar()
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5))
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TeacherHeader()
                    Spacer(modifier = Modifier.height(24.dp))
                    TeacherStatisticsSection()
                    Spacer(modifier = Modifier.height(32.dp))
                    TeacherQuickActions()
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    )
}

// -----------------------------------------------------------------
// DRAWER (igual que todos)
// -----------------------------------------------------------------
@Composable
fun TeacherDrawerContent(onItemClick: () -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text(
            "Menú Profesor",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))

        DrawerItem("Mis Clases", Icons.Filled.Class, onItemClick)
        DrawerItem("Estudiantes", Icons.Filled.School, onItemClick)
        DrawerItem("Calificaciones", Icons.Filled.Grade, onItemClick)
        DrawerItem("Asistencia", Icons.Filled.HowToReg, onItemClick)
        DrawerItem("Horarios", Icons.Filled.Schedule, onItemClick)

        Spacer(modifier = Modifier.height(40.dp))
        Divider(color = Color.Gray.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))

        DrawerItem(
            title = "Cerrar Sesión",
            icon = Icons.Filled.Logout,
            onClick = onLogout,
            textColor = Color(0xFFE57373),
            iconTint = Color(0xFFE57373)
        )
    }
}

@Composable
fun DrawerItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    textColor: Color = Color.White,
    iconTint: Color = Color.Gray
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

// -----------------------------------------------------------------
// TOP BAR
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeacherTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text("Panel del Profesor", fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Menú", tint = Color.Gray)
            }
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones")
            }
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Person, contentDescription = "Perfil")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

// -----------------------------------------------------------------
// BOTTOM BAR
// -----------------------------------------------------------------
@Composable
private fun TeacherBottomBar() {
    BottomAppBar(containerColor = Color.White, modifier = Modifier.height(56.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomIcon("clases", "clases", {}, Icons.Filled.Class)
            BottomIcon("estudiantes", "estudiantes", {}, Icons.Filled.School)
            BottomIcon("calificaciones", "calificaciones", {}, Icons.Filled.Grade)
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
            tint = if (selected == item) Color(0xFFFF9800) else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
    }
}

// -----------------------------------------------------------------
// HEADER
// -----------------------------------------------------------------
@Composable
private fun TeacherHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MatriTech", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Panel del Profesor", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFF9800)) {
            Text(
                "PROFESOR",
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
private fun TeacherStatisticsSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("CLASES ASIGNADAS", "6", Icons.Filled.Class, Color(0xFFFF9800))
            StatCard("ESTUDIANTES", "184", Icons.Filled.School, Color(0xFFFF7043))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("PENDIENTES", "12", Icons.Filled.Warning, Color(0xFFF44336))
            StatCard("HOY", "4 Clases", Icons.Filled.Today, Color(0xFF2196F3))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Card(
        modifier = Modifier.height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = iconColor.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, color = Color.Black.copy(alpha = 0.7f))
        }
    }
}

// -----------------------------------------------------------------
// ACCIONES RÁPIDAS
// -----------------------------------------------------------------
@Composable
private fun TeacherQuickActions() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Acciones Rápidas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard("Registrar Asistencia", Icons.Filled.HowToReg, Color(0xFFFF9800))
            QuickActionCard("Subir Calificaciones", Icons.Filled.Grade, Color.White, textColor = Color.Black)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard("Ver Horario", Icons.Filled.Schedule, Color.White, textColor = Color.Black)
            QuickActionCard("Enviar Comunicado", Icons.Filled.Send, Color(0xFF2196F3))
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    textColor: Color = Color.White
) {
    Card(
        modifier = Modifier.height(100.dp).clickable { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (backgroundColor == Color.White) Color(0xFFFF9800) else Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
        }
    }
}

// -----------------------------------------------------------------
// PREVIEW
// -----------------------------------------------------------------
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun TeacherScreenPreview() {
    TeacherScreen(navController = androidx.navigation.compose.rememberNavController())
}