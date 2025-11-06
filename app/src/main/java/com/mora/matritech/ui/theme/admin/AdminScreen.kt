package com.mora.matritech.ui.theme.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.mora.matritech.ui.theme.MatriTechTheme
import com.mora.matritech.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen() {
    var selectedItem by remember { mutableStateOf("home") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { /* abrir menú lateral */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu),
                            contentDescription = "Menú",
                            tint = Color.Unspecified // <--- para que el vector mantenga su color
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* notificaciones */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notifications),
                            contentDescription = "Notificaciones",
                            tint = Color.Unspecified
                        )
                    }
                    IconButton(onClick = { /* perfil */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_user),
                            contentDescription = "Perfil",
                            tint = Color.Unspecified
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFF00C3FF)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedItem = "home" }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home),
                            contentDescription = "Inicio",
                            tint = if (selectedItem == "home") Color.Unspecified else Color.White
                        )
                    }

                    IconButton(onClick = { selectedItem = "add" }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "Agregar",
                            tint = if (selectedItem == "add") Color.Unspecified else Color.White
                        )
                    }

                    IconButton(onClick = { selectedItem = "chat" }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat),
                            contentDescription = "Mensajes",
                            tint = if (selectedItem == "chat") Color.Unspecified else Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Tarjetas del panel principal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AdminCard(title = "Inscritos", iconId = R.drawable.ic_user)
                AdminCard(title = "Pendientes", iconId = R.drawable.ic_pending)
                AdminCard(title = "Grados", iconId = R.drawable.ic_section)
            }
        }
    }
}

@Composable
fun AdminCard(title: String, iconId: Int) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(120.dp)
            .clickable { /* acción según sección */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF7FF))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = title,
                tint = Color.Unspecified, // <--- mantiene color del vector
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAdminScreen() {
    MatriTechTheme {
        AdminScreen()
    }
}

