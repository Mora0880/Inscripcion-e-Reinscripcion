package com.mora.matritech.screens.superadmin

import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mora.matritech.model.Institucion
import com.mora.matritech.model.InstitucionEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitucionesScreen(
    viewModel: InstitucionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Instituciones") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(InstitucionEvent.ShowDialog) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Institución")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error ?: "",
                        onDismiss = { viewModel.onEvent(InstitucionEvent.ClearError) },
                        onRetry = { viewModel.onEvent(InstitucionEvent.LoadInstituciones(true)) }
                    )
                }
                uiState.instituciones.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    InstitucionesList(
                        instituciones = uiState.instituciones,
                        onEdit = { viewModel.onEvent(InstitucionEvent.SelectInstitucion(it)) },
                        onDelete = { viewModel.onEvent(InstitucionEvent.DeleteInstitucion(it.id.orEmpty())) }
                    )
                }
            }

            if (uiState.showDialog) {
                InstitucionDialog(
                    institucion = uiState.institucionSeleccionada,
                    onDismiss = { viewModel.onEvent(InstitucionEvent.HideDialog) },
                    onSave = { institucion ->
                        if (uiState.institucionSeleccionada != null) {
                            viewModel.onEvent(InstitucionEvent.UpdateInstitucion(institucion))
                        } else {
                            viewModel.onEvent(InstitucionEvent.CreateInstitucion(institucion))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InstitucionesList(
    instituciones: List<Institucion>,
    onEdit: (Institucion) -> Unit,
    onDelete: (Institucion) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(instituciones) { institucion ->
            InstitucionCard(
                institucion = institucion,
                onEdit = { onEdit(institucion) },
                onDelete = { onDelete(institucion) }
            )
        }
    }
}

@Composable
fun InstitucionCard(
    institucion: Institucion,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = institucion.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Provincia y Dirección
                    if (!institucion.provincia.isNullOrBlank()) {
                        Text(
                            text = institucion.provincia,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!institucion.direccion.isNullOrBlank()) {
                        Text(
                            text = institucion.direccion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Info adicional
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        if (!institucion.codigoIdentificacion.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Tag,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = institucion.codigoIdentificacion,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (institucion.anoLaboracion != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = institucion.anoLaboracion.toString(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (!institucion.contacto.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = institucion.contacto,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Tipo y Nivel
                    if (!institucion.tipoInstitucion.isNullOrBlank() || !institucion.nivelEducativo.isNullOrBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            if (!institucion.tipoInstitucion.isNullOrBlank()) {
                                AssistChip(
                                    onClick = { },
                                    label = { Text(institucion.tipoInstitucion, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                            if (!institucion.nivelEducativo.isNullOrBlank()) {
                                AssistChip(
                                    onClick = { },
                                    label = { Text(institucion.nivelEducativo, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Institución") },
            text = { Text("¿Estás seguro de eliminar '${institucion.nombre}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "No hay instituciones registradas",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Presiona el botón + para agregar una",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}