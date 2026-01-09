package com.mora.matritech.screens.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mora.matritech.model.UserRole
import com.mora.matritech.screens.admin.users.components.UserCard
import com.mora.matritech.screens.admin.users.components.UserFilterChip

/**
 * Pantalla principal de gestión de usuarios
 * Para el rol de Administrador
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: UserViewModel = viewModel(),
    onNavigateToForm: (userId: String?) -> Unit,
    currentUserRoleId: Int? = null // ← NUEVO: Rol del usuario actual
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Filtrar Super Admins si el usuario es Admin (no Super Admin)
    val displayUsers = remember(uiState.filteredUsers, currentUserRoleId) {
        if (currentUserRoleId == 1) { // Si es Admin normal (no Super Admin)
            uiState.filteredUsers.filter { it.roleId != 0 } // Ocultar Super Admins
        } else {
            uiState.filteredUsers // Super Admin ve todo
        }
    }

    // Mostrar mensajes
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToForm(null) },
                containerColor = Color(0xFF2196F3)
            ) {
                Icon(Icons.Default.Add, "Agregar usuario", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            // Header compacto con estadísticas
            UserManagementHeader(stats = uiState.stats)

            // Contenido principal
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Barra de búsqueda dentro de la lista
                    item {
                        SearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = viewModel::searchUsers
                        )
                    }

                    // Filtros dentro de la lista
                    item {
                        FilterSection(
                            selectedRole = uiState.selectedRoleFilter,
                            showActiveOnly = uiState.showActiveOnly,
                            onRoleFilterChange = viewModel::filterByRole,
                            onToggleActive = viewModel::toggleActiveFilter,
                            currentUserRoleId = currentUserRoleId
                        )
                    }

                    // Lista de usuarios
                    if (displayUsers.isEmpty()) {
                        item {
                            EmptyState()
                        }
                    } else {
                        items(displayUsers, key = { it.id }) { user ->
                            UserCard(
                                user = user,
                                onEdit = { onNavigateToForm(user.id) },
                                onToggleActive = {
                                    if (user.activo) {
                                        viewModel.deactivateUser(user.id)
                                    } else {
                                        viewModel.activateUser(user.id)
                                    }
                                }
                            )
                        }
                    }

                    // Espaciado al final para el FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

// ==================== HEADER COMPACTO ====================

@Composable
private fun UserManagementHeader(stats: com.mora.matritech.data.repository.UserStats?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Gestión de Usuarios",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            stats?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatChip(
                        label = "Total",
                        value = it.totalUsers,
                        color = Color(0xFF2196F3)
                    )
                    StatChip(
                        label = "Activos",
                        value = it.activeUsers,
                        color = Color(0xFF4CAF50)
                    )
                    StatChip(
                        label = "Inactivos",
                        value = it.inactiveUsers,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.StatChip(label: String, value: Int, color: Color) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

// ==================== BÚSQUEDA ====================

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    var text by remember { mutableStateOf(query) }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onQueryChange(it)
        },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Buscar por nombre o email...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = {
                    text = ""
                    onQueryChange("")
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Color.Gray)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        )
    )
}

// ==================== FILTROS ====================

@Composable
private fun FilterSection(
    selectedRole: Int?,
    showActiveOnly: Boolean,
    onRoleFilterChange: (Int?) -> Unit,
    onToggleActive: () -> Unit,
    currentUserRoleId: Int?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "Filtros",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filtro por rol (sin scroll horizontal, se ajusta automáticamente)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            UserFilterChip(
                label = "Todos",
                selected = selectedRole == null,
                onClick = { onRoleFilterChange(null) }
            )

            // Filtrar roles según el usuario actual
            UserRole.values().forEach { role ->
                // Si es Admin (roleId = 1), NO mostrar Super Admin (roleId = 0)
                val shouldShow = if (currentUserRoleId == 1) {
                    role.id != 0 // Ocultar Super Admin
                } else {
                    true // Super Admin ve todo
                }

                if (shouldShow) {
                    UserFilterChip(
                        label = role.displayName,
                        selected = selectedRole == role.id,
                        onClick = { onRoleFilterChange(role.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filtro activo/inactivo
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = showActiveOnly,
                onCheckedChange = { onToggleActive() }
            )
            Text("Solo usuarios activos", fontSize = 14.sp)
        }
    }
}

// ==================== ESTADO VACÍO ====================

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "No se encontraron usuarios",
                fontSize = 15.sp,
                color = Color.Gray
            )
        }
    }
}

// ==================== FLOWROW (Para wrapping de chips) ====================

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val sequences = mutableListOf<List<Placeable>>()
        val currentSequence = mutableListOf<Placeable>()
        var currentWidth = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)

            if (currentWidth + placeable.width > constraints.maxWidth) {
                sequences.add(currentSequence.toList())
                currentSequence.clear()
                currentWidth = 0
            }

            currentSequence.add(placeable)
            currentWidth += placeable.width + 6.dp.roundToPx()
        }

        if (currentSequence.isNotEmpty()) {
            sequences.add(currentSequence.toList())
        }

        var yPosition = 0

        layout(constraints.maxWidth, sequences.sumOf { row ->
            row.maxOf { it.height } + 6.dp.roundToPx()
        }) {
            sequences.forEach { row ->
                var xPosition = 0
                val rowHeight = row.maxOf { it.height }

                row.forEach { placeable ->
                    placeable.placeRelative(x = xPosition, y = yPosition)
                    xPosition += placeable.width + 6.dp.roundToPx()
                }

                yPosition += rowHeight + 6.dp.roundToPx()
            }
        }
    }
}

@Composable
private fun Layout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    measurePolicy: androidx.compose.ui.layout.MeasurePolicy
) {
    androidx.compose.ui.layout.Layout(
        modifier = modifier,
        content = content,
        measurePolicy = measurePolicy
    )
}