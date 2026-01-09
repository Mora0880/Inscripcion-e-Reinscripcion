package com.mora.matritech.screens.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mora.matritech.model.UserRole
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Info

/**
 * Pantalla de formulario para crear o editar usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    userId: String? = null, // null = crear, not null = editar
    viewModel: UserViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()  // ← Necesario para detectar successMessage
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar datos si es edición
    LaunchedEffect(userId) {
        if (userId != null) {
            // En un caso real, cargarías el usuario por ID
            // Por ahora el ViewModel ya debe tener preparado el formulario
            // desde UserManagementScreen con prepareEditUser()
        } else {
            viewModel.prepareCreateUser()
        }
    }

    // Mostrar errores
    LaunchedEffect(formState.error) {
        formState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFormError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (formState.isEditing) "Editar Usuario" else "Nuevo Usuario",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nombre
            OutlinedTextField(
                value = formState.nombre,
                onValueChange = viewModel::updateFormNombre,
                label = { Text("Nombre completo") },
                enabled = !formState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Email
            OutlinedTextField(
                value = formState.email,
                onValueChange = viewModel::updateFormEmail,
                label = { Text("Correo electrónico") },
                enabled = !formState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Selector de rol
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    if (!formState.isLoading) expanded = !expanded
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = formState.selectedRole.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rol") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    enabled = !formState.isLoading,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    UserRole.values().forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.displayName) },
                            onClick = {
                                viewModel.updateFormRole(role)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Contraseña (solo al crear)
            if (!formState.isEditing) {
                OutlinedTextField(
                    value = formState.password,
                    onValueChange = viewModel::updateFormPassword,
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !formState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        Text(
                            "Mínimo 6 caracteres",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                )

                OutlinedTextField(
                    value = formState.confirmPassword,
                    onValueChange = viewModel::updateFormConfirmPassword,
                    label = { Text("Confirmar contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !formState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.cancelForm()
                        onNavigateBack()
                    },
                    enabled = !formState.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        if (formState.isEditing) {
                            viewModel.updateUser()
                        } else {
                            viewModel.createUser()
                        }
                    },
                    enabled = !formState.isLoading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    if (formState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (formState.isEditing) "Guardar" else "Crear Usuario")
                    }
                }
            }

            // Info adicional solo al editar
            if (formState.isEditing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF9C4)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFF57C00)
                        )
                        Text(
                            "No se puede cambiar la contraseña desde aquí. El usuario debe usar la opción de recuperación de contraseña.",
                            fontSize = 13.sp,
                            color = Color(0xFF6D4C41)
                        )
                    }
                }
            }
        }
    }

    // ← CORRECCIÓN: Volver atrás solo cuando hay un mensaje de éxito
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            onNavigateBack()
        }
    }
}