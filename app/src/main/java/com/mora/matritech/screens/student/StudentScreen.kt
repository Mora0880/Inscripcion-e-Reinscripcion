package com.mora.matritech.screens.student

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
import androidx.navigation.NavHostController
import com.mora.matritech.data.local.SessionManager
import com.mora.matritech.ui.theme.NavRoutes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    navController: NavHostController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawerContent(
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
                    StudentTopBar(onMenuClick = { scope.launch { drawerState.open() } })
                },
                bottomBar = {
                    StudentBottomBar()
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
                    StudentHeader()
                    Spacer(modifier = Modifier.height(24.dp))
                    StudentStatisticsSection()
                    Spacer(modifier = Modifier.height(32.dp))
                    StudentQuickActions()
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    )
}

// -----------------------------------------------------------------
// DRAWER
// -----------------------------------------------------------------
@Composable
fun StudentDrawerContent(onItemClick: () -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text(
            "Menú Estudiante",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))

        DrawerItem("Inicio", Icons.Filled.Home, onItemClick)
        DrawerItem("Mis Notas", Icons.Filled.Grade, onItemClick)
        DrawerItem("Asistencia", Icons.Filled.HowToReg, onItemClick)
        DrawerItem("Horario", Icons.Filled.Schedule, onItemClick)
        DrawerItem("Comunicados", Icons.Filled.Mail, onItemClick)
        DrawerItem("Pagos", Icons.Filled.Payment, onItemClick)

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
private fun StudentTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text("Panel del Estudiante", fontWeight = FontWeight.SemiBold) },
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
private fun StudentQuickActions() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Acciones Rápidas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard("Ver Notas", Icons.Filled.Grade, Color(0xFF00ACC1))
            QuickActionCard("Mi Horario", Icons.Filled.Schedule, Color.White, textColor = Color.Black)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard("Asistencia", Icons.Filled.HowToReg, Color.White, textColor = Color.Black)
            QuickActionCard("Comunicados", Icons.Filled.Mail, Color(0xFF26A69A))
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
            Icon(icon, contentDescription = null, tint = if (backgroundColor == Color.White) Color(0xFF00ACC1) else Color.White, modifier = Modifier.size(32.dp))
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
fun StudentScreenPreview() {
    StudentScreen(navController = androidx.navigation.compose.rememberNavController())
}