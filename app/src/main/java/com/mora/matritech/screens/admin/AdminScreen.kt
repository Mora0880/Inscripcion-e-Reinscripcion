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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

// -----------------------------------------------------------------
// VIEWMODEL (el que ya tenías, solo lo importamos correctamente)
// -----------------------------------------------------------------
// Ya lo tienes en otro archivo, así que solo lo usamos:
// (Si está en el mismo archivo, déjalo arriba. Si está en otro, quita las clases de abajo)

// -----------------------------------------------------------------
// PANTALLA PRINCIPAL - ACTUALIZADA PARA USAR isDrawerOpen del ViewModel
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState
    val drawerState = rememberDrawerState(
        initialValue = if (uiState.isDrawerOpen) DrawerValue.Open else DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()

    // Sincronizamos el estado del drawer físico con el del ViewModel
    LaunchedEffect(uiState.isDrawerOpen) {
        if (uiState.isDrawerOpen && drawerState.isClosed) {
            drawerState.open()
        } else if (!uiState.isDrawerOpen && drawerState.isOpen) {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminDrawerContent(
                onItemClicked = { viewModel.closeDrawer() }
            )
        },
        content = {
            Scaffold(
                topBar = {
                    AdminTopBar(
                        onMenuClick = { viewModel.openDrawer() }
                    )
                },
                bottomBar = {
                    AdminBottomBar(
                        selectedItem = uiState.selectedBottomItem,
                        onItemSelected = viewModel::onBottomItemSelected
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
                    QuickActionsSection()
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
fun AdminDrawerContent(onItemClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text(
            "Menú",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))

        DrawerItem("Usuarios", Icons.Default.People, onItemClicked)
        DrawerItem("Reportes", Icons.Default.Assessment, onItemClicked)
        DrawerItem("Configuración", Icons.Default.Settings, onItemClicked)
        DrawerItem("Cerrar sesión", Icons.Default.Logout, onItemClicked)
    }
}

@Composable
fun DrawerItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = Color.White)
    }
}

// -----------------------------------------------------------------
// TOP BAR
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text("") },
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
            BottomIcon("add", selectedItem, onItemSelected, Icons.Default.AddCircle)
            BottomIcon("chat", selectedItem, onItemSelected, Icons.Default.Chat)
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
// HEADER, STATS Y ACCIONES RÁPIDAS (sin cambios)
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

@Composable
private fun StatisticsSection(stats: AdminStats) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("TOTAL USUARIOS", stats.totalUsers.toString(), Icons.Default.People, Color(0xFF6B7EFF))
            StatCard("ESTUDIANTES", stats.students.toString(), Icons.Default.School, Color(0xFF4CAF50))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("DOCENTES", stats.teachers.toString(), Icons.Default.Person, Color(0xFFFF9800))
            StatCard("ADMINISTRADORES", stats.admins.toString(), Icons.Default.AdminPanelSettings, Color(0xFFF44336))
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

@Composable
private fun QuickActionsSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Acciones Rápidas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard("Agregar Usuario", Icons.Default.PersonAdd, Color(0xFF2196F3))
            QuickActionCard("Exportar Datos", Icons.Default.Download, Color.White, textColor = Color.Black)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard("Ver Reportes", Icons.Default.Assessment, Color.White, textColor = Color.Black)
            QuickActionCard("Configuración", Icons.Default.Settings, Color.White, textColor = Color.Black)
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
        modifier = Modifier
            .height(100.dp)
            .clickable { },
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
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminScreenPreview() {
    AdminScreen()
}