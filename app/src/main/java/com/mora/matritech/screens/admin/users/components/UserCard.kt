package com.mora.matritech.screens.admin.users.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mora.matritech.model.UserData
import com.mora.matritech.model.UserRole

/**
 * Tarjeta para mostrar información de un usuario
 */
@Composable
fun UserCard(
    user: UserData,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.activo) Color.White else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar y datos
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar
                Surface(
                    shape = CircleShape,
                    color = getRoleColor(user.roleId).copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            user.nombre.take(1).uppercase(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = getRoleColor(user.roleId)
                        )
                    }
                }

                // Información
                Column {
                    Text(
                        user.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (user.activo) Color.Black else Color.Gray
                    )
                    Text(
                        user.email,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge de rol
                        RoleBadge(roleId = user.roleId)

                        // Badge de estado
                        if (!user.activo) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFF9800).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "INACTIVO",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
                    }
                }
            }

            // Menú de acciones
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )

                    Divider()

                    DropdownMenuItem(
                        text = {
                            Text(
                                if (user.activo) "Desactivar" else "Activar",
                                color = if (user.activo) Color(0xFFF44336) else Color(0xFF4CAF50)
                            )
                        },
                        onClick = {
                            showMenu = false
                            showConfirmDialog = true
                        },
                        leadingIcon = {
                            Icon(
                                if (user.activo) Icons.Default.Block else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (user.activo) Color(0xFFF44336) else Color(0xFF4CAF50)
                            )
                        }
                    )
                }
            }
        }
    }

    // Diálogo de confirmación
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(if (user.activo) "¿Desactivar usuario?" else "¿Activar usuario?")
            },
            text = {
                Text(
                    if (user.activo) {
                        "El usuario ${user.nombre} no podrá iniciar sesión hasta que sea activado nuevamente."
                    } else {
                        "El usuario ${user.nombre} podrá iniciar sesión nuevamente."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onToggleActive()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.activo) Color(0xFFF44336) else Color(0xFF4CAF50)
                    )
                ) {
                    Text(if (user.activo) "Desactivar" else "Activar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Badge de rol
 */
@Composable
private fun RoleBadge(roleId: Int?) {
    val role = UserRole.fromId(roleId) ?: return
    val color = getRoleColor(roleId)

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            role.displayName.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Obtener color según el rol
 */
private fun getRoleColor(roleId: Int?): Color {
    return when (roleId) {
        0 -> Color(0xFFE91E63) // Super Admin - Rosa
        1 -> Color(0xFFF44336) // Admin - Rojo
        2 -> Color(0xFFFF9800) // Coordinador - Naranja
        3 -> Color(0xFF2196F3) // Estudiante - Azul
        4 -> Color(0xFF4CAF50) // Docente - Verde
        5 -> Color(0xFF9C27B0) // Representante - Morado
        else -> Color.Gray
    }
}