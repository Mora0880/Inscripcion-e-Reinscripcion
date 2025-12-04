package com.mora.matritech.screens.superadmin

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mora.matritech.data.local.SessionManager
import com.mora.matritech.data.remote.supabase
import com.mora.matritech.data.repository.InstitucionRepository
import com.mora.matritech.data.repository.InstitucionViewModelFactory
import com.mora.matritech.ui.theme.NavRoutes
import kotlinx.coroutines.launch

// Enum para las secciones del SuperAdmin
enum class SuperAdminSection {
    DASHBOARD,
    INSTITUCIONES,
    USUARIOS,
    REPORTES,
    CONFIGURACION
}

// -----------------------------------------------------------------
// PANTALLA PRINCIPAL SUPER ADMIN
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminScreen(
    navController: NavHostController = rememberNavController()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Estado para controlar qué sección mostrar
    var currentSection by remember { mutableStateOf(SuperAdminSection.DASHBOARD) }

    // Crear el repository y viewmodel solo una vez
    val institucionRepository = remember { InstitucionRepository(supabase) }
    val institucionViewModel: InstitucionViewModel = viewModel(
        factory = InstitucionViewModelFactory(institucionRepository)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SuperAdminDrawerContent(
                currentSection = currentSection,
                onItemClick = { section ->
                    currentSection = section
                    scope.launch { drawerState.close() }
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
                    SuperAdminTopBar(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        currentSection = currentSection
                    )
                },
                bottomBar = {
                    if (currentSection == SuperAdminSection.DASHBOARD) {
                        SuperAdminBottomBar(
                            currentSection = currentSection,
                            onSectionChange = { currentSection = it }
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    // Mostrar la sección correspondiente
                    when (currentSection) {
                        SuperAdminSection.DASHBOARD -> DashboardContent()
                        SuperAdminSection.INSTITUCIONES -> {
                            InstitucionesScreen(viewModel = institucionViewModel)
                        }
                        SuperAdminSection.USUARIOS -> UsuariosContent()
                        SuperAdminSection.REPORTES -> ReportesContent()
                        SuperAdminSection.CONFIGURACION -> ConfiguracionContent()
                    }
                }
            }
        }
    )
}

// -----------------------------------------------------------------
// DASHBOARD CONTENT
// -----------------------------------------------------------------
@Composable
private fun DashboardContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SuperAdminHeader()
        Spacer(modifier = Modifier.height(24.dp))
        SuperAdminStatisticsSection()
        Spacer(modifier = Modifier.height(32.dp))
        SuperAdminQuickActions()
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// Placeholders para las otras secciones
@Composable
private fun UsuariosContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF9C27B0)
            )
            Text(
                "Sección de Usuarios",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "En desarrollo",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ReportesContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Assessment,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF673AB7)
            )
            Text(
                "Sección de Reportes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "En desarrollo",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ConfiguracionContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF2196F3)
            )
            Text(
                "Sección de Configuración",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "En desarrollo",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

// -----------------------------------------------------------------
// DRAWER
// -----------------------------------------------------------------
@Composable
fun SuperAdminDrawerContent(
    currentSection: SuperAdminSection,
    onItemClick: (SuperAdminSection) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                "Super Administrador",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Panel de Control",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        DrawerItem(
            title = "Dashboard",
            icon = Icons.Default.Dashboard,
            onClick = { onItemClick(SuperAdminSection.DASHBOARD) },
            isSelected = currentSection == SuperAdminSection.DASHBOARD
        )
        DrawerItem(
            title = "Instituciones",
            icon = Icons.Default.AccountBalance,
            onClick = { onItemClick(SuperAdminSection.INSTITUCIONES) },
            isSelected = currentSection == SuperAdminSection.INSTITUCIONES
        )
        DrawerItem(
            title = "Usuarios Globales",
            icon = Icons.Default.People,
            onClick = { onItemClick(SuperAdminSection.USUARIOS) },
            isSelected = currentSection == SuperAdminSection.USUARIOS
        )
        DrawerItem(
            title = "Reportes",
            icon = Icons.Default.Assessment,
            onClick = { onItemClick(SuperAdminSection.REPORTES) },
            isSelected = currentSection == SuperAdminSection.REPORTES
        )
        DrawerItem(
            title = "Configuración",
            icon = Icons.Default.Settings,
            onClick = { onItemClick(SuperAdminSection.CONFIGURACION) },
            isSelected = currentSection == SuperAdminSection.CONFIGURACION
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
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
    icon: ImageVector,
    onClick: () -> Unit,
    textColor: Color = Color.White,
    iconTint: Color = Color.Gray,
    isSelected: Boolean = false
) {
    val backgroundColor = if (isSelected) Color(0xFFE91E63).copy(alpha = 0.2f) else Color.Transparent
    val itemIconTint = if (isSelected) Color(0xFFE91E63) else iconTint
    val itemTextColor = if (isSelected) Color(0xFFE91E63) else textColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = itemIconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 15.sp, color = itemTextColor, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

// -----------------------------------------------------------------
// TOP BAR
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuperAdminTopBar(
    onMenuClick: () -> Unit,
    currentSection: SuperAdminSection
) {
    val title = when (currentSection) {
        SuperAdminSection.DASHBOARD -> "Panel Super Admin"
        SuperAdminSection.INSTITUCIONES -> "Gestión de Instituciones"
        SuperAdminSection.USUARIOS -> "Usuarios Globales"
        SuperAdminSection.REPORTES -> "Reportes del Sistema"
        SuperAdminSection.CONFIGURACION -> "Configuración"
    }

    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
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
// BOTTOM BAR
// -----------------------------------------------------------------
@Composable
private fun SuperAdminBottomBar(
    currentSection: SuperAdminSection,
    onSectionChange: (SuperAdminSection) -> Unit
) {
    BottomAppBar(containerColor = Color.White, modifier = Modifier.height(56.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomIcon(
                section = SuperAdminSection.DASHBOARD,
                currentSection = currentSection,
                onClick = onSectionChange,
                icon = Icons.Default.Dashboard
            )
            BottomIcon(
                section = SuperAdminSection.INSTITUCIONES,
                currentSection = currentSection,
                onClick = onSectionChange,
                icon = Icons.Default.AccountBalance
            )
            BottomIcon(
                section = SuperAdminSection.CONFIGURACION,
                currentSection = currentSection,
                onClick = onSectionChange,
                icon = Icons.Default.Settings
            )
        }
    }
}

@Composable
private fun BottomIcon(
    section: SuperAdminSection,
    currentSection: SuperAdminSection,
    onClick: (SuperAdminSection) -> Unit,
    icon: ImageVector
) {
    IconButton(onClick = { onClick(section) }) {
        Icon(
            imageVector = icon,
            contentDescription = section.name,
            tint = if (currentSection == section) Color(0xFFE91E63) else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
    }
}

// -----------------------------------------------------------------
// HEADER
// -----------------------------------------------------------------
@Composable
private fun SuperAdminHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MatriTech", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Panel Super Administrador", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFE91E63)) {
            Text(
                "SUPER ADMIN",
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
private fun SuperAdminStatisticsSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "INSTITUCIONES",
                value = "12",
                icon = Icons.Default.AccountBalance,
                iconColor = Color(0xFFE91E63),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "USUARIOS TOTALES",
                value = "2.480",
                icon = Icons.Default.People,
                iconColor = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "ADMINISTRADORES",
                value = "48",
                icon = Icons.Default.AdminPanelSettings,
                iconColor = Color(0xFF673AB7),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "ACTIVAS",
                value = "11",
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp),
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
private fun SuperAdminQuickActions() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Acciones Rápidas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                title = "Nueva Institución",
                icon = Icons.Default.AddBusiness,
                backgroundColor = Color(0xFFE91E63),
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Crear Admin",
                icon = Icons.Default.PersonAdd,
                backgroundColor = Color.White,
                textColor = Color.Black,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                title = "Ver Logs",
                icon = Icons.Default.ReceiptLong,
                backgroundColor = Color.White,
                textColor = Color.Black,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Backup DB",
                icon = Icons.Default.CloudDownload,
                backgroundColor = Color(0xFF00BCD4),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp).clickable { },
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
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// -----------------------------------------------------------------
// PREVIEW
// -----------------------------------------------------------------
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun SuperAdminScreenPreview() {
    SuperAdminScreen()
}