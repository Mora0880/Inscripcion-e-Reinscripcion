package com.mora.matritech.ui.screens.superadmin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mora.matritech.model.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreenCR(
    viewModel: AdminViewModelCR,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var adminToDelete by remember { mutableStateOf<UserData?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Administradores") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(AdminEvent.ShowCreateDialog) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Crear Administrador")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.onEvent(AdminEvent.SearchAdministradores(it))
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre o email...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.onEvent(AdminEvent.SearchAdministradores(""))
                        }) {
                            Icon(Icons.Default.Close, "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Estadísticas rápidas
            AdminStatsCard(
                total = uiState.administradores.size,
                activos = uiState.administradores.count { it.activo },
                inactivos = uiState.administradores.count { !it.activo }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de administradores
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.administradoresFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay administradores",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.administradoresFiltrados) { admin ->
                        AdminCard(
                            admin = admin,
                            instituciones = uiState.instituciones,
                            onEditClick = {
                                viewModel.onEvent(AdminEvent.SelectAdministrador(admin))
                                viewModel.onEvent(AdminEvent.ShowEditDialog)
                            },
                            onDeleteClick = {
                                adminToDelete = admin
                                showDeleteDialog = true
                            },
                            onToggleActivo = { activo ->
                                viewModel.onEvent(AdminEvent.ToggleActivoAdministrador(admin.id, activo))
                            }
                        )
                    }
                }
            }
        }

        // Diálogo de crear/editar
        if (uiState.showDialog) {
            AdminFormDialog(
                mode = uiState.dialogMode,
                admin = uiState.adminSeleccionado,
                instituciones = uiState.instituciones,
                isLoading = uiState.isLoading,
                error = uiState.error,
                onDismiss = { viewModel.onEvent(AdminEvent.HideDialog) },
                onConfirm = { formData ->
                    if (uiState.dialogMode == DialogMode.CREATE) {
                        viewModel.onEvent(AdminEvent.CreateAdministrador(formData))
                    } else {
                        viewModel.onEvent(AdminEvent.UpdateAdministrador(formData))
                    }
                }
            )
        }

        // Diálogo de confirmación de eliminación
        if (showDeleteDialog && adminToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar eliminación") },
                text = {
                    Text("¿Estás seguro de que deseas eliminar al administrador ${adminToDelete?.nombre}?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onEvent(AdminEvent.DeleteAdministrador(adminToDelete!!.id))
                            showDeleteDialog = false
                            adminToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Snackbar para errores y éxitos
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                kotlinx.coroutines.delay(3000)
                viewModel.onEvent(AdminEvent.ClearError)
            }
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(error)
            }
        }

        uiState.successMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3000)
                viewModel.onEvent(AdminEvent.ClearSuccess)
            }
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(message)
            }
        }
    }
}

@Composable
fun AdminStatsCard(
    total: Int,
    activos: Int,
    inactivos: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Total", total.toString(), MaterialTheme.colorScheme.primary)
            StatItem("Activos", activos.toString(), Color(0xFF4CAF50))
            StatItem("Inactivos", inactivos.toString(), Color(0xFFFF9800))
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun AdminCard(
    admin: UserData,
    instituciones: List<com.mora.matritech.model.Institucion>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleActivo: (Boolean) -> Unit
) {
    val institucion = instituciones.find { it.id == admin.institucionId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (admin.activo)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con inicial
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = admin.nombre.firstOrNull()?.uppercase() ?: "A",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = admin.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = admin.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                institucion?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = it.nombre,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Badge de estado
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = if (admin.activo) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (admin.activo) "Activo" else "Inactivo",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            // Menú de acciones
            var expanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, "Opciones")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            expanded = false
                            onEditClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(if (admin.activo) "Desactivar" else "Activar") },
                        onClick = {
                            expanded = false
                            onToggleActivo(!admin.activo)
                        },
                        leadingIcon = {
                            Icon(
                                if (admin.activo) Icons.Default.Lock else Icons.Default.CheckCircle,
                                null
                            )
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            expanded = false
                            onDeleteClick()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}