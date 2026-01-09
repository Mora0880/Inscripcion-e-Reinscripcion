package com.mora.matritech.screens.admin

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

// -----------------------------------------------------------------
// PANTALLA PRINCIPAL - CON NAVEGACIÓN AL CRUD
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavHostController,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Sincronizar drawer con ViewModel
    LaunchedEffect(uiState.isDrawerOpen) {
        if (uiState.isDrawerOpen) drawerState.open() else drawerState.close()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminDrawerContent(
                onItemClick = { item ->
                    scope.launch { drawerState.close() }
                    viewModel.closeDrawer()

                    // Navegar según el item
                    when (item) {
                        "usuarios" -> navController.navigate("admin/users")
                        "dashboard" -> navController.navigate("admin/dashboard")
                        "reportes" -> navController.navigate("admin/reports")
                        "configuracion" -> navController.navigate("admin/settings")
                    }
                },
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
                    AdminTopBar(onMenuClick = { viewModel.openDrawer() })
                },
                bottomBar = {
                    AdminBottomBar(
                        selectedItem = uiState.selectedBottomItem,
                        onItemSelected = { item ->
                            viewModel.onBottomItemSelected(item)
                            when (item) {
                                "users" -> navController.navigate("admin/users")
                                "reports" -> navController.navigate("admin/reports")
                            }
                        }
                    )
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
                    AdminHeader()
                    Spacer(modifier = Modifier.height(24.dp))
                    StatisticsSection(stats = uiState.stats)
                    Spacer(modifier = Modifier.height(32.dp))
                    QuickActionsSection(
                        onAddUser = { navController.navigate("admin/users/form") },
                        onViewReports = { navController.navigate("admin/reports") },
                        onViewSettings = { navController.navigate("admin/settings") }
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    )
}

// -----------------------------------------------------------------
// DRAWER CON NAVEGACIÓN
// -----------------------------------------------------------------
@Composable
fun AdminDrawerContent(
    onItemClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text(
            "Menú Administrador",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(32.dp))

        DrawerItem(
            title = "Dashboard",
            icon = Icons.Default.Dashboard,
            onClick = { onItemClick("dashboard") }
        )

        DrawerItem(
            title = "Usuarios",
            icon = Icons.Default.People,
            onClick = { onItemClick("usuarios") }
        )

        DrawerItem(
            title = "Reportes",
            icon = Icons.Default.Assessment,
            onClick = { onItemClick("reportes") }
        )

        DrawerItem(
            title = "Configuración",
            icon = Icons.Default.Settings,
            onClick = { onItemClick("configuracion") }
        )

        DrawerItem(
            title = "Notificaciones",
            icon = Icons.Default.Notifications,
            onClick = { onItemClick("notificaciones") }
        )

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
// TOP BAR
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text("Panel de Administración", fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Abrir menú", tint = Color.Gray)
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
// BOTTOM BAR
// -----------------------------------------------------------------
@Composable
private fun AdminBottomBar(selectedItem: String, onItemSelected: (String) -> Unit) {
    BottomAppBar(containerColor = Color.White, modifier = Modifier.height(56.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomIcon("home", selectedItem, onItemSelected, Icons.Default.Home)
            BottomIcon("users", selectedItem, onItemSelected, Icons.Default.People)
            BottomIcon("reports", selectedItem, onItemSelected, Icons.Default.Assessment)
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
            tint = if (selected == item) Color(0xFF2196F3) else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
    }
}

// -----------------------------------------------------------------
// HEADER
// -----------------------------------------------------------------
@Composable
private fun AdminHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MatriTech", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Panel de Administración", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF6B7EFF)) {
            Text(
                "ADMINISTRADOR",
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
private fun StatisticsSection(stats: AdminStats) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "TOTAL USUARIOS",
                value = stats.totalUsers.toString(),
                icon = Icons.Default.People,
                iconColor = Color(0xFF6B7EFF)
            )
            StatCard(
                title = "ESTUDIANTES",
                value = stats.students.toString(),
                icon = Icons.Default.School,
                iconColor = Color(0xFF4CAF50)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "DOCENTES",
                value = stats.teachers.toString(),
                icon = Icons.Default.Person,
                iconColor = Color(0xFFFF9800)
            )
            StatCard(
                title = "ADMINISTRADORES",
                value = stats.admins.toString(),
                icon = Icons.Default.AdminPanelSettings,
                iconColor = Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun RowScope.StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(130.dp),
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
// ACCIONES RÁPIDAS CON NAVEGACIÓN
// -----------------------------------------------------------------
@Composable
private fun QuickActionsSection(
    onAddUser: () -> Unit,
    onViewReports: () -> Unit,
    onViewSettings: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Acciones Rápidas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                title = "Agregar Usuario",
                icon = Icons.Default.PersonAdd,
                backgroundColor = Color(0xFF2196F3),
                textColor = Color.White,
                onClick = onAddUser
            )
            QuickActionCard(
                title = "Exportar Datos",
                icon = Icons.Default.Download,
                backgroundColor = Color.White,
                textColor = Color.Black,
                onClick = { }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                title = "Ver Reportes",
                icon = Icons.Default.Assessment,
                backgroundColor = Color.White,
                textColor = Color.Black,
                onClick = onViewReports
            )
            QuickActionCard(
                title = "Configuración",
                icon = Icons.Default.Settings,
                backgroundColor = Color.White,
                textColor = Color.Black,
                onClick = onViewSettings
            )
        }
    }
}

@Composable
private fun RowScope.QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(100.dp)
            .clickable { onClick() },
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