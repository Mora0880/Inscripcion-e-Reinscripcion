package com.mora.matritech.screens.student

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.mora.matritech.data.local.SessionManager
import com.mora.matritech.ui.theme.NavRoutes

// ============================================================================
// PANTALLA PRINCIPAL - StudentScreen
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Obtener userId de la sesión
    val userId = sessionManager.getUserId() ?: run {
        // Si no hay userId, redirigir al login
        LaunchedEffect(Unit) {
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    // Crear ViewModel con contexto y userId
    val viewModel: StudentViewModel = remember {
        StudentViewModel(context, userId)
    }

    // Estados del ViewModel
    val currentStep by viewModel.currentStep.collectAsState()
    val enrollmentUiState by viewModel.enrollmentUiState.collectAsState()

    // Control de navegación entre vista dashboard e inscripción
    var showEnrollmentForm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (showEnrollmentForm) "Inscripción" else "Panel de Estudiante")
                },
                navigationIcon = {
                    if (showEnrollmentForm) {
                        IconButton(onClick = {
                            showEnrollmentForm = false
                            viewModel.resetForm()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            sessionManager.logout()
                            navController.navigate(NavRoutes.Login.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (!showEnrollmentForm) {
                StudentBottomBar()
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showEnrollmentForm) {
                // Mostrar formulario de inscripción
                EnrollmentFormScreen(
                    viewModel = viewModel,
                    onEnrollmentComplete = {
                        showEnrollmentForm = false
                        viewModel.resetForm()
                    }
                )
            } else {
                // Mostrar dashboard del estudiante
                StudentDashboard(
                    onStartEnrollment = { showEnrollmentForm = true }
                )
            }
        }
    }
}

// ============================================================================
// DASHBOARD DEL ESTUDIANTE
// ============================================================================

@Composable
private fun StudentDashboard(
    onStartEnrollment: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        StudentHeader()
        Spacer(modifier = Modifier.height(24.dp))
        StudentStatisticsSection()
        Spacer(modifier = Modifier.height(24.dp))

        // Botón de inscripción
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onStartEnrollment() },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00ACC1)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Nueva Inscripción",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Inscríbete para el próximo período",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
                Icon(
                    imageVector = Icons.Filled.AppRegistration,
                    contentDescription = "Inscribirse",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ============================================================================
// FORMULARIO DE INSCRIPCIÓN
// ============================================================================

@Composable
private fun EnrollmentFormScreen(
    viewModel: StudentViewModel,
    onEnrollmentComplete: () -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val enrollmentUiState by viewModel.enrollmentUiState.collectAsState()
    val formData by viewModel.formData.collectAsState()

    // Manejar estados de éxito/error
    when (val state = enrollmentUiState) {
        is EnrollmentUiState.Success -> {
            SuccessDialog(
                message = state.message,
                onDismiss = {
                    viewModel.clearEnrollmentState()
                    onEnrollmentComplete()
                }
            )
        }
        is EnrollmentUiState.Error -> {
            ErrorSnackbar(message = state.message)
        }
        else -> {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Stepper visual
        StepperIndicator(
            currentStep = currentStep,
            totalSteps = viewModel.totalSteps,
            onStepClick = { step -> viewModel.goToStep(step) }
        )

        // Contenido del paso actual
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentStep) {
                0 -> PersonalDataStep(viewModel, formData)
                1 -> DocumentsStep(viewModel, formData)
                2 -> ConfirmationStep(viewModel, formData)
            }

            // Loading overlay
            if (enrollmentUiState is EnrollmentUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        // Botones de navegación
        NavigationButtons(
            currentStep = currentStep,
            totalSteps = viewModel.totalSteps,
            isLoading = enrollmentUiState is EnrollmentUiState.Loading,
            onPrevious = { viewModel.previousStep() },
            onNext = { viewModel.nextStep() },
            onSubmit = { viewModel.submitEnrollment() }
        )
    }
}

// ============================================================================
// STEPPER INDICATOR
// ============================================================================

@Composable
private fun StepperIndicator(
    currentStep: Int,
    totalSteps: Int,
    onStepClick: (Int) -> Unit
) {
    val steps = listOf(
        StepInfo(0, "Datos Personales", Icons.Filled.Person),
        StepInfo(1, "Documentos", Icons.Filled.Description),
        StepInfo(2, "Confirmación", Icons.Filled.CheckCircle)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, step ->
                StepItem(
                    step = step,
                    isActive = index == currentStep,
                    isCompleted = index < currentStep,
                    onClick = { if (index <= currentStep) onStepClick(index) }
                )

                if (index < steps.size - 1) {
                    StepConnector(isCompleted = index < currentStep)
                }
            }
        }
    }
}

@Composable
private fun RowScope.StepItem(
    step: StepInfo,
    isActive: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isActive -> Color(0xFF00ACC1)
                        isCompleted -> Color(0xFF26A69A)
                        else -> Color(0xFFE0E0E0)
                    }
                )
                .clickable(enabled = isCompleted || isActive) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Filled.Check else step.icon,
                contentDescription = step.title,
                tint = if (isActive || isCompleted) Color.White else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = step.title,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Color(0xFF00ACC1) else Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RowScope.StepConnector(isCompleted: Boolean) {
    Divider(
        modifier = Modifier
            .weight(0.5f)
            .padding(horizontal = 4.dp)
            .align(Alignment.CenterVertically),
        color = if (isCompleted) Color(0xFF26A69A) else Color(0xFFE0E0E0),
        thickness = 2.dp
    )
}

data class StepInfo(val index: Int, val title: String, val icon: ImageVector)

// ============================================================================
// PASO 1: DATOS PERSONALES
// ============================================================================

@Composable
private fun PersonalDataStep(
    viewModel: StudentViewModel,
    formData: EnrollmentFormData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Datos Personales",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Completa tus datos personales para continuar con la inscripción",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = formData.nombre,
            onValueChange = { viewModel.updatePersonalData(nombre = it) },
            label = { Text("Nombre *") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = formData.apellido,
            onValueChange = { viewModel.updatePersonalData(apellido = it) },
            label = { Text("Apellido *") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = formData.documentoIdentidad,
            onValueChange = { viewModel.updatePersonalData(documentoIdentidad = it) },
            label = { Text("Documento de Identidad *") },
            leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = formData.fechaNacimiento,
            onValueChange = { viewModel.updatePersonalData(fechaNacimiento = it) },
            label = { Text("Fecha de Nacimiento (DD/MM/YYYY) *") },
            leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("01/01/2010") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = formData.telefono,
            onValueChange = { viewModel.updatePersonalData(telefono = it) },
            label = { Text("Teléfono *") },
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = formData.email,
            onValueChange = { viewModel.updatePersonalData(email = it) },
            label = { Text("Email *") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "* Campos obligatorios",
            fontSize = 12.sp,
            color = Color.Gray,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(80.dp)) // Espacio para los botones
    }
}

// ============================================================================
// PASO 2: DOCUMENTOS
// ============================================================================

@Composable
private fun DocumentsStep(
    viewModel: StudentViewModel,
    formData: EnrollmentFormData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Documentos Requeridos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Carga los documentos necesarios para completar tu inscripción",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        DocumentUploadCard(
            title = "Foto del DNI",
            description = "Imagen clara de ambos lados del documento",
            icon = Icons.Filled.CreditCard,
            selectedUri = formData.dniDocumento,
            onDocumentSelected = { uri -> viewModel.updateDocuments(dniUri = uri) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        DocumentUploadCard(
            title = "Acta de Nacimiento",
            description = "Documento oficial en formato PDF o imagen",
            icon = Icons.Filled.Description,
            selectedUri = formData.actaNacimiento,
            onDocumentSelected = { uri -> viewModel.updateDocuments(actaNacimientoUri = uri) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        DocumentUploadCard(
            title = "Certificado Académico",
            description = "Constancia de estudios previos",
            icon = Icons.Filled.School,
            selectedUri = formData.certificadoAcademico,
            onDocumentSelected = { uri -> viewModel.updateDocuments(certificadoAcademicoUri = uri) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Los documentos deben ser legibles y en formato PDF o imagen (JPG, PNG)",
                    fontSize = 12.sp,
                    color = Color(0xFF5D4037)
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Espacio para los botones
    }
}

@Composable
private fun DocumentUploadCard(
    title: String,
    description: String,
    icon: ImageVector,
    selectedUri: Uri?,
    onDocumentSelected: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onDocumentSelected(it) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { launcher.launch("*/*") },
        colors = CardDefaults.cardColors(
            containerColor = if (selectedUri != null) Color(0xFFE8F5E9) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (selectedUri != null) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (selectedUri != null) Icons.Filled.CheckCircle else icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedUri != null) "✓ Documento cargado" else description,
                    fontSize = 13.sp,
                    color = if (selectedUri != null) Color(0xFF4CAF50) else Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Filled.Upload,
                contentDescription = "Cargar",
                tint = Color(0xFF00ACC1),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ============================================================================
// PASO 3: CONFIRMACIÓN
// ============================================================================

@Composable
private fun ConfirmationStep(
    viewModel: StudentViewModel,
    formData: EnrollmentFormData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Confirmación de Datos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Revisa que todos los datos sean correctos antes de enviar",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Datos Personales
        SectionCard(title = "Datos Personales") {
            ConfirmationRow("Nombre", formData.nombre)
            ConfirmationRow("Apellido", formData.apellido)
            ConfirmationRow("Documento", formData.documentoIdentidad)
            ConfirmationRow("Fecha de Nacimiento", formData.fechaNacimiento)
            ConfirmationRow("Teléfono", formData.telefono)
            ConfirmationRow("Email", formData.email)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Documentos
        SectionCard(title = "Documentos Cargados") {
            DocumentConfirmationRow("DNI", formData.dniDocumento != null)
            DocumentConfirmationRow("Acta de Nacimiento", formData.actaNacimiento != null)
            DocumentConfirmationRow("Certificado Académico", formData.certificadoAcademico != null)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Una vez enviada la inscripción, tus datos serán revisados por un administrador. Recibirás una notificación cuando tu inscripción sea aprobada.",
                    fontSize = 12.sp,
                    color = Color(0xFF1565C0)
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Espacio para los botones
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00ACC1),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun ConfirmationRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DocumentConfirmationRow(label: String, isUploaded: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isUploaded) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                contentDescription = null,
                tint = if (isUploaded) Color(0xFF4CAF50) else Color(0xFFE57373),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isUploaded) "Cargado" else "No cargado",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isUploaded) Color(0xFF4CAF50) else Color(0xFFE57373)
            )
        }
    }
}

// ============================================================================
// BOTONES DE NAVEGACIÓN
// ============================================================================

@Composable
private fun NavigationButtons(
    currentStep: Int,
    totalSteps: Int,
    isLoading: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón Atrás
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Atrás")
                }
            }

            // Botón Siguiente/Enviar
            Button(
                onClick = if (currentStep == totalSteps - 1) onSubmit else onNext,
                modifier = Modifier.weight(if (currentStep > 0) 1f else 1f),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00ACC1)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (currentStep == totalSteps - 1) "Enviar Inscripción" else "Siguiente")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (currentStep == totalSteps - 1)
                            Icons.Filled.Send else Icons.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// DIÁLOGOS Y SNACKBARS
// ============================================================================

@Composable
private fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "¡Éxito!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = message,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Entendido")
            }
        }
    )
}

@Composable
private fun ErrorSnackbar(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = Color(0xFFC62828),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFFC62828)
            )
        }
    }
}

// ============================================================================
// COMPONENTES DEL DASHBOARD (Funcionalidad existente)
// ============================================================================

@Composable
private fun StudentBottomBar() {
    BottomAppBar(
        containerColor = Color.White,
        modifier = Modifier.height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomIcon("inicio", "inicio", {}, Icons.Filled.Home)
            BottomIcon("notas", "notas", {}, Icons.Filled.Grade)
            BottomIcon("horario", "horario", {}, Icons.Filled.Schedule)
        }
    }
}

@Composable
private fun BottomIcon(
    item: String,
    selected: String,
    onClick: (String) -> Unit,
    icon: ImageVector
) {
    IconButton(onClick = { onClick(item) }) {
        Icon(
            imageVector = icon,
            contentDescription = item,
            tint = if (selected == item) Color(0xFF00ACC1) else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun StudentHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "MatriTech",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Panel del Estudiante",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF00ACC1)
        ) {
            Text(
                "ESTUDIANTE",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = title,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentStatisticsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                "PROMEDIO GENERAL",
                "8.7",
                Icons.Filled.Star,
                Color(0xFF00ACC1),
                Modifier.weight(1f)
            )
            StatCard(
                "ASISTENCIA",
                "94%",
                Icons.Filled.HowToReg,
                Color(0xFF26A69A),
                Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                "MATERIAS",
                "8",
                Icons.Filled.Book,
                Color(0xFF0097A7),
                Modifier.weight(1f)
            )
            StatCard(
                "FALTAS",
                "3",
                Icons.Filled.Warning,
                Color(0xFFFF9800),
                Modifier.weight(1f)
            )
        }
    }
}