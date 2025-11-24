package com.mora.matritech.screens.representante

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mora.matritech.R
import com.mora.matritech.ui.theme.MatriTechTheme
import kotlinx.coroutines.launch




// ----------------------------------------------------
// CARD INDIVIDUAL DE ESTADÍSTICA
// ----------------------------------------------------
@Composable
fun StatCard(
    icon: ImageVector,
    number: String,
    label: String,
    background: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {

        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF1A237E),
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(number, fontSize = 22.sp, color = Color(0xFF1A237E))
        Text(label, fontSize = 14.sp, color = Color.Gray)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepresentanteScreen() {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedBottom by remember { mutableStateOf("home") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(250.dp)
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Text("Menú", fontSize = 22.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Text("Inicio", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(14.dp))
                Text("Configuraciones", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(14.dp))
                Text("Cerrar Sesión", fontSize = 18.sp)
            }
        }
    ) {
        Scaffold(

            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "menu",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Notifications, null, Modifier.size(35.dp))
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.AccountCircle, null, Modifier.size(35.dp))
                        }
                    }
                )
            },

            bottomBar = {
                NavigationBar(containerColor = Color.White) {

                    NavigationBarItem(
                        selected = selectedBottom == "home",
                        onClick = { selectedBottom = "home" },
                        icon = { Icon(Icons.Default.Home, null, Modifier.size(28.dp)) },
                        label = { Text("Inicio") }
                    )

                    NavigationBarItem(
                        selected = selectedBottom == "hijos",
                        onClick = { selectedBottom = "hijos" },
                        icon = { Icon(Icons.Default.AccountCircle, null, Modifier.size(28.dp)) },
                        label = { Text("Hijos") }
                    )

                    NavigationBarItem(
                        selected = selectedBottom == "ajustes",
                        onClick = { selectedBottom = "ajustes" },
                        icon = { Icon(Icons.Default.Settings, null, Modifier.size(28.dp)) },
                        label = { Text("Ajustes") }
                    )
                }
            }

        ) { innerPadding ->

            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ----------------------------------------------------
                // ENCABEZADO CON DEGRADADO + TÍTULO
                // ----------------------------------------------------
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF1A237E),
                                        Color(0xFF3F51B5)
                                    )
                                )
                            )
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {



                            Spacer(modifier = Modifier.height(15.dp))

                            Text(
                                "Panel del Representante",
                                fontSize = 22.sp,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // ----------------------------------------------------
                    // TARJETA DE ESTADÍSTICAS
                    // ----------------------------------------------------
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                "Resumen Académico",
                                fontSize = 20.sp,
                                color = Color(0xFF1A237E)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatCard(
                                    icon = Icons.Default.Build,
                                    number = "7%",
                                    label = "Materias",
                                    background = Color(0xFFE3F2FD)
                                )

                                StatCard(
                                    icon = Icons.Default.Build,
                                    number = "8",
                                    label = "Clases/sem",
                                    background = Color(0xFFE8F5E9)
                                )

                                StatCard(
                                    icon = Icons.Default.Star,
                                    number = "13",
                                    label = "Notas",
                                    background = Color(0xFFFFF3E0)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }

                // ----------------------------------------------------
                // LISTA DE HIJOS
                // ----------------------------------------------------
                items(listOf("Juan Pérez", "Ana López", "María Torres")) { name ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8EAF6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_background),
                                    contentDescription = null,
                                    modifier = Modifier.size(35.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(15.dp))

                            Column {
                                Text(name, fontSize = 18.sp)
                                Text("Ver detalles", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(50.dp)) }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RepresentanteScreenPreview() {
    MatriTechTheme {
        RepresentanteScreen()
    }
}
