package com.mora.matritech.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.mora.matritech.data.local.SessionManager
import com.mora.matritech.data.repository.Inscripcion

// ============================================================================
// PANTALLA PRINCIPAL - LISTA DE INSCRIPCIONES
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEnrollmentsScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val adminId = sessionManager.getUserId() ?: run {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val viewModel = remember { AdminEnrollmentsViewModel(context, adminId) }

    val uiState by viewModel.uiState.collectAsState()
    val enrollments by viewModel.enrollments.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val actionDialogState by viewModel.actionDialogState.collectAsState()

    // Manejar diálogos
    when (val state = actionDialogState) {
        is ActionDialogState.Approve -> {
            ApproveDialog(
                inscripcion = state.inscripcion,
                onConfirm = {
                    viewModel.approveEnrollment(state.inscripcion.id!!)
                },
                onDismiss = { viewModel.hideActionDialog() }
            )
        }
        is ActionDialogState.Reject -> {
            RejectDialog(
                inscripcion = state.inscripcion,
                onConfirm = { motivo ->
                    viewModel.rejectEnrollment(state.inscripcion.id!!, motivo)
                },
                onDismiss = { viewModel.hideActionDialog() }
            )
        }
        ActionDialogState.Hidden -> { /* No mostrar diálogo */ }
    }

    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is EnrollmentsUiState.ActionSuccess -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearMessage()
            }
            is EnrollmentsUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearMessage()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Inscripciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadEnrollments() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            // Estadísticas rápidas
            EnrollmentStatsCard(viewModel.getEnrollmentStats())

            Spacer(modifier = Modifier.height(8.dp))

            // Filtros
            FilterChips(
                selectedFilter = filterState,
                onFilterSelected = { viewModel.setFilter(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de inscripciones
            when (uiState) {
                is EnrollmentsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    if (enrollments.isEmpty()) {
                        EmptyState(filterState)
                    } else {
                        EnrollmentsList(
                            enrollments = enrollments,
                            onEnrollmentClick = { inscripcion ->
                                navController.navigate("admin/enrollments/detail/${inscripcion.id}")
                            },
                            onApprove = { viewModel.showApproveDialog(it) },
                            onReject = { viewModel.showRejectDialog(it) }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// ESTADÍSTICAS
// ============================================================================

@Composable
private fun EnrollmentStatsCard(stats: EnrollmentStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Total", stats.total.toString(), Color(0xFF2196F3))
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = Color.Gray.copy(alpha = 0.3f)
            )
            StatItem("Pendientes", stats.pendientes.toString(), Color(0xFFFF9800))
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = Color.Gray.copy(alpha = 0.3f)
            )
            StatItem("Aprobadas", stats.aprobadas.toString(), Color(0xFF4CAF50))
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = Color.Gray.copy(alpha = 0.3f)
            )
            StatItem("Rechazadas", stats.rechazadas.toString(), Color(0xFFE57373))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}

// ============================================================================
// FILTROS
// ============================================================================

@Composable
private fun FilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            label = "Todas",
            selected = selectedFilter == "todas",
            onClick = { onFilterSelected("todas") }
        )
        FilterChip(
            label = "Pendientes",
            selected = selectedFilter == "pendiente",
            onClick = { onFilterSelected("pendiente") }
        )
        FilterChip(
            label = "Aprobadas",
            selected = selectedFilter == "aprobada",
            onClick = { onFilterSelected("aprobada") }
        )
        FilterChip(
            label = "Rechazadas",
            selected = selectedFilter == "rechazada",
            onClick = { onFilterSelected("rechazada") }
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFF2196F3) else Color.White,
        border = if (!selected) androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.Gray.copy(alpha = 0.3f)
        ) else null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (selected) Color.White else Color.Gray,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ============================================================================
// LISTA DE INSCRIPCIONES
// ============================================================================

@Composable
private fun EnrollmentsList(
    enrollments: List<Inscripcion>,
    onEnrollmentClick: (Inscripcion) -> Unit,
    onApprove: (Inscripcion) -> Unit,
    onReject: (Inscripcion) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(enrollments) { inscripcion ->
            EnrollmentCard(
                inscripcion = inscripcion,
                onClick = { onEnrollmentClick(inscripcion) },
                onApprove = { onApprove(inscripcion) },
                onReject = { onReject(inscripcion) }
            )
        }
    }
}

@Composable
private fun EnrollmentCard(
    inscripcion: Inscripcion,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header con estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = getEstadoColor(inscripcion.estado).copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = getEstadoColor(inscripcion.estado),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${inscripcion.nombre} ${inscripcion.apellido}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "DNI: ${inscripcion.documento_identidad}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                EstadoBadge(inscripcion.estado)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(Icons.Default.Email, inscripcion.email)
                InfoItem(Icons.Default.Phone, inscripcion.telefono)
            }

            Spacer(modifier = Modifier.height(8.dp))

            InfoItem(Icons.Default.CalendarToday, "Nacimiento: ${inscripcion.fecha_nacimiento}")

            // Fecha de solicitud
            if (inscripcion.fecha_solicitud != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Solicitado: ${formatDate(inscripcion.fecha_solicitud)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Botones de acción (solo para pendientes)
            if (inscripcion.estado == "pendiente") {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE57373)
                        )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar")
                    }

                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprobar")
                    }
                }
            }

            // Motivo de rechazo
            if (inscripcion.estado == "rechazada" && !inscripcion.motivo_rechazo.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Motivo del rechazo:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Text(
                                inscripcion.motivo_rechazo,
                                fontSize = 12.sp,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EstadoBadge(estado: String) {
    val (color, text) = when (estado) {
        "pendiente" -> Color(0xFFFF9800) to "PENDIENTE"
        "aprobada" -> Color(0xFF4CAF50) to "APROBADA"
        "rechazada" -> Color(0xFFE57373) to "RECHAZADA"
        else -> Color.Gray to estado.uppercase()
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================================
// ESTADO VACÍO
// ============================================================================

@Composable
private fun EmptyState(filter: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay inscripciones ${if (filter == "todas") "" else filter + "s"}",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

// ============================================================================
// DIÁLOGOS
// ============================================================================

@Composable
private fun ApproveDialog(
    inscripcion: Inscripcion,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text("Aprobar Inscripción")
        },
        text = {
            Text(
                "¿Estás seguro de aprobar la inscripción de " +
                        "${inscripcion.nombre} ${inscripcion.apellido}?\n\n" +
                        "Esta acción creará el registro del estudiante en el sistema."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Aprobar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun RejectDialog(
    inscripcion: Inscripcion,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var motivo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Cancel,
                contentDescription = null,
                tint = Color(0xFFE57373),
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text("Rechazar Inscripción")
        },
        text = {
            Column {
                Text(
                    "¿Estás seguro de rechazar la inscripción de " +
                            "${inscripcion.nombre} ${inscripcion.apellido}?"
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo del rechazo *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Explica el motivo...") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (motivo.isNotBlank()) {
                        onConfirm(motivo)
                    }
                },
                enabled = motivo.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE57373)
                )
            ) {
                Text("Rechazar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// ============================================================================
// UTILIDADES
// ============================================================================

private fun getEstadoColor(estado: String): Color {
    return when (estado) {
        "pendiente" -> Color(0xFFFF9800)
        "aprobada" -> Color(0xFF4CAF50)
        "rechazada" -> Color(0xFFE57373)
        else -> Color.Gray
    }
}

private fun formatDate(dateString: String): String {
    return try {
        // Formato básico, puedes mejorarlo
        dateString.substringBefore("T").split("-").reversed().joinToString("/")
    } catch (e: Exception) {
        dateString
    }
}