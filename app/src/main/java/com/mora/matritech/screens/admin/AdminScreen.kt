package com.mora.matritech.screens.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: AdminViewModel = viewModel()) {
    val uiState by viewModel.uiState
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminDrawerContent(
                onItemClicked = {
                    viewModel.closeDrawer()
                    scope.launch {
                        if (drawerState.isOpen) drawerState.close()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                AdminTopBar(
                    onMenuClick = {
                        viewModel.openDrawer()
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open()
                        }
                    }
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
}
/* -----------------------------
   DRAWER
 ----------------------------- */

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
            color = Color.White          // 👈 Cambiar texto a blanco
        )
        Spacer(modifier = Modifier.height(24.dp))

        DrawerItem("Usuarios", Icons.Default.People, onItemClicked)
        DrawerItem("Reportes", Icons.Default.Assessment, onItemClicked)
        DrawerItem("Configuración", Icons.Default.Settings, onItemClicked)
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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 16.sp, color = Color.White)
    }
}

/* -----------------------------
   TOP BAR
 ----------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text("") },
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

/* -----------------------------
   BOTTOM BAR
 ----------------------------- */

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
    IconButton(onClick = { onClick(item) }, modifier = Modifier.size(40.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = item,
            tint = if (selected == item) Color(0xFF2196F3) else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
    }
}

/* -----------------------------
   HEADER
 ----------------------------- */

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

/* -----------------------------
   STATS
 ----------------------------- */

@Composable
private fun StatisticsSection(stats: AdminStats) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("TOTAL USUARIOS", stats.totalUsers.toString(), Icons.Default.People, Color(0xFF6B7EFF))
            StatCard("ESTUDIANTES", stats.students.toString(), Icons.Default.School, Color(0xFF4CAF50))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 10.sp, color = Color.Black.copy(alpha = 0.7f))
        }
    }
}

/* -----------------------------
   ACCIONES RÁPIDAS
 ----------------------------- */

@Composable
private fun QuickActionsSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Acciones Rápidas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard("Agregar Usuario", Icons.Default.PersonAdd, Color(0xFF2196F3))
            QuickActionCard("Exportar Datos", Icons.Default.Download, Color.White, textColor = Color.Black)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp).clickable {},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (backgroundColor == Color.White) Color(0xFF2196F3) else Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminScreenView() {
    AdminScreen()
}
