package com.mora.matritech.screens.coordinator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun CoordinatorScreen(
    viewModel: CoordinatorViewModel = viewModel(),
    navController: NavHostController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val groups by viewModel.groups.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadGroups() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CoordinatorDrawerContent(
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
                topBar = { CoordinatorTopBar { scope.launch { drawerState.open() } } },
                bottomBar = { CoordinatorBottomBar() }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5))
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CoordinatorHeader()
                    Spacer(modifier = Modifier.height(24.dp))
                    CoordinatorStatisticsSection(groups.size)
                    Spacer(modifier = Modifier.height(32.dp))
                    CoordinatorQuickActions()
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Grupos Asignados",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(groups) { group -> GroupCard(group) }
                    }
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
fun CoordinatorDrawerContent(onItemClick: () -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text("Menú Coordinador", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))

        DrawerItem("Dashboard", Icons.Filled.Dashboard, onItemClick)
        DrawerItem("Grupos", Icons.Filled.GroupWork, onItemClick)
        DrawerItem("Estudiantes", Icons.Filled.School, onItemClick)
        DrawerItem("Reportes", Icons.Filled.Assessment, onItemClick)
        DrawerItem("Horarios", Icons.Filled.Schedule, onItemClick)

        Spacer(modifier = Modifier.height(40.dp))
        Divider(color = Color.Gray.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))

        DrawerItem("Cerrar Sesión", Icons.Filled.Logout, onLogout, textColor = Color(0xFFE57373), iconTint = Color(0xFFE57373))
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
private fun CoordinatorTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text("Panel del Coordinador", fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Menú", tint = Color.Gray)
            }
        },
        actions = {
            IconButton(onClick = { }) { Icon(Icons.Filled.Notifications, "Notificaciones") }
            IconButton(onClick = { }) { Icon(Icons.Filled.Person, "Perfil") }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

// -----------------------------------------------------------------
// BOTTOM BAR
// -----------------------------------------------------------------
@Composable
private fun CoordinatorBottomBar() {
    BottomAppBar(containerColor = Color.White, modifier = Modifier.height(56.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomIcon("dashboard", "dashboard", {}, Icons.Filled.Dashboard)
            BottomIcon("grupos", "grupos", {}, Icons.Filled.GroupWork)
            BottomIcon("reportes", "reportes", {}, Icons.Filled.Assessment)
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
        Icon(icon, contentDescription = item, tint = if (selected == item) Color(0xFF1565C0) else Color.Gray, modifier = Modifier.size(28.dp))
    }
}

// -----------------------------------------------------------------
// HEADER
// -----------------------------------------------------------------
@Composable
private fun CoordinatorHeader() {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MatriTech", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Panel del Coordinador", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF1565C0)) {
            Text("COORDINADOR", modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// -----------------------------------------------------------------
// ESTADÍSTICAS Y ACCIONES (mismos iconos válidos)
// -----------------------------------------------------------------
@Composable
private fun CoordinatorStatisticsSection(totalGroups: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("GRUPOS ASIGNADOS", totalGroups.toString(), Icons.Filled.GroupWork, Color(0xFF1565C0))
            StatCard("ESTUDIANTES", "285", Icons.Filled.School, Color(0xFF1976D2))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("PENDIENTES", "3", Icons.Filled.Warning, Color(0xFFFF9800))
            StatCard("ACTIVOS", "98%", Icons.Filled.CheckCircle, Color(0xFF4CAF50))
        }
    }
}

@Composable
private fun CoordinatorQuickActions() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Acciones Rápidas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard("Registrar Asistencia", Icons.Filled.HowToReg, Color(0xFF1565C0)) // EXISTE
            QuickActionCard("Reportes Académicos", Icons.Filled.Assessment, Color.White, textColor = Color.Black)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard("Horarios", Icons.Filled.Schedule, Color.White, textColor = Color.Black)
            QuickActionCard("Notificar Padres", Icons.Filled.NotificationsActive, Color(0xFF1976D2)) // EXISTE
        }
    }
}

// Resto de componentes (StatCard, QuickActionCard, GroupCard) igual que antes...
// (Los dejo tal cual, funcionan perfecto)

@Composable
private fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
    Card(modifier = Modifier.height(130.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(shape = CircleShape, color = iconColor.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 11.sp, color = Color.Black.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun QuickActionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, backgroundColor: Color, textColor: Color = Color.White) {
    Card(modifier = Modifier.height(100.dp).clickable { }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(backgroundColor), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (backgroundColor == Color.White) Color(0xFF1565C0) else Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
        }
    }
}

@Composable
fun GroupCard(group: Group) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color(0xFFE3F2FD), modifier = Modifier.size(50.dp)) {
                Icon(Icons.Filled.GroupWork, null, tint = Color(0xFF1565C0), modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("${group.grade}º Año - Sección ${group.section}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Estudiantes inscritos: ${group.enrolled}", color = Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, null, tint = Color.Gray)
        }
    }
}