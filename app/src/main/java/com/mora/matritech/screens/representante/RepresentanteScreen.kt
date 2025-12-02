package com.mora.matritech.screens.representante

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
fun RepresentanteScreen(
    navController: NavHostController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RepresentanteDrawerContent(
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
                    RepresentanteTopBar(onMenuClick = { scope.launch { drawerState.open() } })
                },
                bottomBar = {
                    RepresentanteBottomBar()
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
                    RepresentanteHeader()
                    Spacer(modifier = Modifier.height(24.dp))
                    RepresentanteStatisticsSection()
                    Spacer(modifier = Modifier.height(32.dp))
                    RepresentanteQuickActions()
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    )
}

// -----------------------------------------------------------------
// DRAWER (idéntico al del Admin)
// -----------------------------------------------------------------
@Composable
fun RepresentanteDrawerContent(onItemClick: () -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text(
            "Menú Representante",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))

        DrawerItem("Inicio", Icons.Default.Home, onItemClick)
        DrawerItem("Mis Hijos", Icons.Default.People, onItemClick)
        DrawerItem("Calificaciones", Icons.Default.School, onItemClick)
        DrawerItem("Asistencia", Icons.Default.EventNote, onItemClick)
        DrawerItem("Comunicados", Icons.Default.Mail, onItemClick)
        DrawerItem("Pagos", Icons.Default.Payment, onItemClick)

        Spacer(modifier = Modifier.height(40.dp))
        Divider(color = Color.Gray.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))

        DrawerItem(
            title = "Cerrar Sesión",
            icon = Icons.Default.Logout,
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
// TOP BAR (igual que Admin)
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepresentanteTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text("Panel del Representante", fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.Gray)
            }
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Person, contentDescription = "Perfil")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

// -----------------------------------------------------------------
// BOTTOM BAR (3 íconos como el Admin)
// -----------------------------------------------------------------
@Composable
private fun RepresentanteBottomBar() {
    BottomAppBar(containerColor = Color.White, modifier = Modifier.height(56.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomIcon("home", "home", {}, Icons.Default.Home)
            BottomIcon("hijos", "hijos", {}, Icons.Default.People)
            BottomIcon("pagos", "pagos", {}, Icons.Default.Payment)
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
            tint = if (selected == item) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
    }
}

// -----------------------------------------------------------------
// HEADER (igual que Admin, pero verde)
// -----------------------------------------------------------------
@Composable
private fun RepresentanteHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MatriTech", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Panel del Representante", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF4CAF50)) {
            Text(
                "REPRESENTANTE",
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
private fun RepresentanteStatisticsSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("HIJOS INSCRITOS", "3", Icons.Default.People, Color(0xFF4CAF50))
            StatCard("PENDIENTES", "2", Icons.Default.Warning, Color(0xFFFF9800))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("NOTAS BAJAS", "4", Icons.Default.TrendingDown, Color(0xFFF44336))
            StatCard("EVENTOS", "8", Icons.Default.Event, Color(0xFF2196F3))
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
private fun RepresentanteQuickActions() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Acciones Rápidas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard("Ver Calificaciones", Icons.Default.School, Color(0xFF4CAF50))
            QuickActionCard("Justificar Faltas", Icons.Default.NoteAdd, Color.White, textColor = Color.Black)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard("Comunicados", Icons.Default.Mail, Color.White, textColor = Color.Black)
            QuickActionCard("Realizar Pago", Icons.Default.Payment, Color(0xFF2196F3))
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
            Icon(
                icon,
                contentDescription = null,
                tint = if (backgroundColor == Color.White) Color(0xFF2196F3) else Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
        }
    }
}

// -----------------------------------------------------------------
// PREVIEW
// -----------------------------------------------------------------
// @androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
// @Composable
// fun RepresentanteScreenPreview() {
   //  RepresentanteScreen(navController = androidx.navigation.compose.rememberNavController())
// }