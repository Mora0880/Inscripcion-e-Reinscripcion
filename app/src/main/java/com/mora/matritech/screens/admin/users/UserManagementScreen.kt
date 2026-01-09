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
    onNavigateToForm: (userId: String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
            // Header con estadísticas
            UserManagementHeader(stats = uiState.stats)

            // Barra de búsqueda
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::searchUsers,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filtros
            FilterSection(
                selectedRole = uiState.selectedRoleFilter,
                showActiveOnly = uiState.showActiveOnly,
                onRoleFilterChange = viewModel::filterByRole,
                onToggleActive = viewModel::toggleActiveFilter
            )

            // Lista de usuarios
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredUsers.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredUsers, key = { it.id }) { user ->
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
            }
        }
    }
}

// ==================== HEADER ====================

@Composable
private fun UserManagementHeader(stats: com.mora.matritech.data.repository.UserStats?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Gestión de Usuarios",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            stats?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
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
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

// ==================== BÚSQUEDA ====================

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(query) }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onQueryChange(it)
        },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Buscar por nombre o email...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = {
                    text = ""
                    onQueryChange("")
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Limpiar")
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
    onToggleActive: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            "Filtros",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filtro por rol
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserFilterChip(
                label = "Todos",
                selected = selectedRole == null,
                onClick = { onRoleFilterChange(null) }
            )

            UserRole.values().forEach { role ->
                UserFilterChip(
                    label = role.displayName,
                    selected = selectedRole == role.id,
                    onClick = { onRoleFilterChange(role.id) }
                )
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
            Text("Solo usuarios activos")
        }
    }
}

// ==================== ESTADO VACÍO ====================

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No se encontraron usuarios",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}