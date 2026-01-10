package com.mora.matritech.screens.student

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mora.matritech.data.repository.EnrollmentRepository
import com.mora.matritech.data.repository.EnrollmentResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel para gestionar el panel del estudiante y la inscripción
 * Maneja tanto el dashboard como el formulario de inscripción en pasos
 */
class StudentViewModel(
    private val context: Context,
    private val userId: String
) : ViewModel() {

    private val enrollmentRepository = EnrollmentRepository(context)

    // ========================================================================
    // ESTADO DEL DASHBOARD (Funcionalidad existente)
    // ========================================================================

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses

    fun loadCourses() {
        viewModelScope.launch {
            // Datos de prueba por ahora
            _courses.value = listOf(
                Course(name = "Matemáticas", grade = "A", teacher = "Juan Pérez"),
                Course(name = "Historia", grade = "B", teacher = "María Gómez"),
                Course(name = "Ciencias", grade = "A", teacher = "Carlos López")
            )
        }
    }

    // ========================================================================
    // ESTADO DE INSCRIPCIÓN (Nueva funcionalidad)
    // ========================================================================

    // Estado general de la UI de inscripción
    private val _enrollmentUiState = MutableStateFlow<EnrollmentUiState>(EnrollmentUiState.Editing)
    val enrollmentUiState: StateFlow<EnrollmentUiState> = _enrollmentUiState.asStateFlow()

    // Datos del formulario
    private val _formData = MutableStateFlow(EnrollmentFormData())
    val formData: StateFlow<EnrollmentFormData> = _formData.asStateFlow()

    // Paso actual del formulario (0-indexed: 0=Datos, 1=Documentos, 2=Confirmación)
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // Total de pasos en el formulario
    val totalSteps = 3

    // ========================================================================
    // FUNCIONES DE ACTUALIZACIÓN DE DATOS
    // ========================================================================

    /**
     * Actualiza los datos personales del estudiante
     * Solo actualiza los campos que no son null
     */
    fun updatePersonalData(
        nombre: String? = null,
        apellido: String? = null,
        documentoIdentidad: String? = null,
        fechaNacimiento: String? = null,
        telefono: String? = null,
        email: String? = null
    ) {
        _formData.value = _formData.value.copy(
            nombre = nombre ?: _formData.value.nombre,
            apellido = apellido ?: _formData.value.apellido,
            documentoIdentidad = documentoIdentidad ?: _formData.value.documentoIdentidad,
            fechaNacimiento = fechaNacimiento ?: _formData.value.fechaNacimiento,
            telefono = telefono ?: _formData.value.telefono,
            email = email ?: _formData.value.email
        )
    }

    /**
     * Actualiza los documentos cargados
     */
    fun updateDocuments(
        dniUri: Uri? = null,
        actaNacimientoUri: Uri? = null,
        certificadoAcademicoUri: Uri? = null
    ) {
        _formData.value = _formData.value.copy(
            dniDocumento = dniUri ?: _formData.value.dniDocumento,
            actaNacimiento = actaNacimientoUri ?: _formData.value.actaNacimiento,
            certificadoAcademico = certificadoAcademicoUri ?: _formData.value.certificadoAcademico
        )
    }

    // ========================================================================
    // NAVEGACIÓN ENTRE PASOS
    // ========================================================================

    /**
     * Avanza al siguiente paso si las validaciones son correctas
     */
    fun nextStep() {
        val currentStepValue = _currentStep.value

        when (currentStepValue) {
            0 -> {
                // Validar datos personales antes de avanzar
                val validation = validatePersonalData()
                if (validation.isValid) {
                    _currentStep.value = currentStepValue + 1
                    _enrollmentUiState.value = EnrollmentUiState.Editing
                } else {
                    showError(validation.message)
                }
            }
            1 -> {
                // Validar documentos antes de avanzar
                val validation = validateDocuments()
                if (validation.isValid) {
                    _currentStep.value = currentStepValue + 1
                    _enrollmentUiState.value = EnrollmentUiState.Editing
                } else {
                    showError(validation.message)
                }
            }
            2 -> {
                // En el último paso, no hay siguiente
                // El botón será "Enviar" en lugar de "Siguiente"
            }
        }
    }

    /**
     * Retrocede al paso anterior
     */
    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value -= 1
            _enrollmentUiState.value = EnrollmentUiState.Editing
        }
    }

    /**
     * Navega directamente a un paso específico (para el stepper clickeable)
     * Solo permite ir a pasos anteriores o al actual
     */
    fun goToStep(step: Int) {
        if (step in 0.._currentStep.value && step < totalSteps) {
            _currentStep.value = step
            _enrollmentUiState.value = EnrollmentUiState.Editing
        }
    }

    // ========================================================================
    // VALIDACIONES
    // ========================================================================

    /**
     * Valida los datos personales del paso 1
     */
    private fun validatePersonalData(): ValidationResult {
        val data = _formData.value

        Log.d("StudentViewModel", "Validando datos: $data")

        // Validar nombre
        if (data.nombre.isBlank()) {
            Log.d("StudentViewModel", "Error: nombre vacío")
            return ValidationResult(false, "El nombre es requerido")
        }
        if (data.nombre.length < 2) {
            Log.d("StudentViewModel", "Error: nombre muy corto")
            return ValidationResult(false, "El nombre debe tener al menos 2 caracteres")
        }
        if (!data.nombre.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))) {
            Log.d("StudentViewModel", "Error: nombre con caracteres inválidos")
            return ValidationResult(false, "El nombre solo debe contener letras")
        }

        // Validar apellido
        if (data.apellido.isBlank()) {
            Log.d("StudentViewModel", "Error: apellido vacío")
            return ValidationResult(false, "El apellido es requerido")
        }
        if (data.apellido.length < 2) {
            Log.d("StudentViewModel", "Error: apellido muy corto")
            return ValidationResult(false, "El apellido debe tener al menos 2 caracteres")
        }
        if (!data.apellido.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))) {
            Log.d("StudentViewModel", "Error: apellido con caracteres inválidos")
            return ValidationResult(false, "El apellido solo debe contener letras")
        }

        // Validar documento de identidad
        if (data.documentoIdentidad.isBlank()) {
            Log.d("StudentViewModel", "Error: documento vacío")
            return ValidationResult(false, "El documento de identidad es requerido")
        }
        if (!data.documentoIdentidad.matches(Regex("^[0-9]{7,11}$"))) {
            Log.d("StudentViewModel", "Error: documento formato inválido: ${data.documentoIdentidad}")
            return ValidationResult(false, "El documento debe tener entre 7 y 11 dígitos")
        }

        // Validar fecha de nacimiento
        if (data.fechaNacimiento.isBlank()) {
            Log.d("StudentViewModel", "Error: fecha vacía")
            return ValidationResult(false, "La fecha de nacimiento es requerida")
        }

        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateFormat.isLenient = false // Estricto con el formato
            val birthDate = dateFormat.parse(data.fechaNacimiento)

            if (birthDate == null) {
                Log.d("StudentViewModel", "Error: fecha null después de parse")
                return ValidationResult(false, "Formato de fecha inválido. Use DD/MM/YYYY")
            }

            val birthCalendar = Calendar.getInstance()
            birthCalendar.time = birthDate

            val today = Calendar.getInstance()

            // Verificar que no sea fecha futura
            if (birthCalendar.after(today)) {
                Log.d("StudentViewModel", "Error: fecha futura")
                return ValidationResult(false, "La fecha de nacimiento no puede ser futura")
            }

            // Calcular edad
            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

            // Ajustar si aún no ha cumplido años este año
            val todayMonth = today.get(Calendar.MONTH)
            val birthMonth = birthCalendar.get(Calendar.MONTH)
            val todayDay = today.get(Calendar.DAY_OF_MONTH)
            val birthDay = birthCalendar.get(Calendar.DAY_OF_MONTH)

            if (todayMonth < birthMonth || (todayMonth == birthMonth && todayDay < birthDay)) {
                age--
            }

            Log.d("StudentViewModel", "Edad calculada: $age años")

            if (age < 5) {
                Log.d("StudentViewModel", "Error: menor de 5 años")
                return ValidationResult(false, "El estudiante debe tener al menos 5 años")
            }
            if (age > 100) {
                Log.d("StudentViewModel", "Error: edad mayor a 100")
                return ValidationResult(false, "Por favor verifica la fecha de nacimiento")
            }

        } catch (e: Exception) {
            Log.e("StudentViewModel", "Error parseando fecha: ${e.message}")
            return ValidationResult(false, "Formato de fecha inválido. Use DD/MM/YYYY")
        }

        // Validar teléfono
        if (data.telefono.isBlank()) {
            Log.d("StudentViewModel", "Error: teléfono vacío")
            return ValidationResult(false, "El teléfono es requerido")
        }
        if (!data.telefono.matches(Regex("^[0-9]{10,15}$"))) {
            Log.d("StudentViewModel", "Error: teléfono formato inválido: ${data.telefono}")
            return ValidationResult(false, "El teléfono debe tener entre 10 y 15 dígitos")
        }

        // Validar email
        if (data.email.isBlank()) {
            Log.d("StudentViewModel", "Error: email vacío")
            return ValidationResult(false, "El email es requerido")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(data.email).matches()) {
            Log.d("StudentViewModel", "Error: email formato inválido: ${data.email}")
            return ValidationResult(false, "El formato del email no es válido")
        }

        Log.d("StudentViewModel", "✅ Validación exitosa")
        return ValidationResult(true, "")
    }

    /**
     * Valida los documentos del paso 2
     */
    private fun validateDocuments(): ValidationResult {
        val data = _formData.value

        if (data.dniDocumento == null) {
            return ValidationResult(false, "Debe cargar la foto del DNI")
        }

        if (data.actaNacimiento == null) {
            return ValidationResult(false, "Debe cargar el acta de nacimiento")
        }

        if (data.certificadoAcademico == null) {
            return ValidationResult(false, "Debe cargar el certificado académico")
        }

        return ValidationResult(true, "")
    }

    // ========================================================================
    // ENVÍO DEL FORMULARIO
    // ========================================================================

    /**
     * Envía el formulario de inscripción
     * Integra con Supabase: Database + Storage
     */
    fun submitEnrollment() {
        viewModelScope.launch {
            try {
                _enrollmentUiState.value = EnrollmentUiState.Loading

                Log.d("StudentViewModel", "🚀 Iniciando envío de inscripción...")

                val data = _formData.value

                // Validar que todos los datos estén completos
                if (data.dniDocumento == null ||
                    data.actaNacimiento == null ||
                    data.certificadoAcademico == null) {
                    throw Exception("Faltan documentos por cargar")
                }

                // Llamar al repository para crear la inscripción
                val result = enrollmentRepository.createEnrollment(
                    nombre = data.nombre,
                    apellido = data.apellido,
                    documentoIdentidad = data.documentoIdentidad,
                    fechaNacimiento = data.fechaNacimiento,
                    telefono = data.telefono,
                    email = data.email,
                    dniUri = data.dniDocumento!!,
                    actaNacimientoUri = data.actaNacimiento!!,
                    certificadoAcademicoUri = data.certificadoAcademico!!,
                    userId = userId
                )

                when (result) {
                    is EnrollmentResult.Success -> {
                        Log.d("StudentViewModel", "✅ Inscripción exitosa: ${result.inscripcionId}")
                        _enrollmentUiState.value = EnrollmentUiState.Success(
                            "¡Inscripción enviada exitosamente!\n\n" +
                                    "ID de inscripción: ${result.inscripcionId.take(8)}...\n\n" +
                                    "Tus datos y documentos han sido guardados correctamente. " +
                                    "Un administrador revisará tu solicitud y recibirás una notificación " +
                                    "cuando tu inscripción sea aprobada."
                        )
                    }
                    is EnrollmentResult.Error -> {
                        Log.e("StudentViewModel", "❌ Error en inscripción: ${result.message}")
                        _enrollmentUiState.value = EnrollmentUiState.Error(
                            "Error al enviar la inscripción: ${result.message}"
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e("StudentViewModel", "❌ Excepción en submitEnrollment: ${e.message}", e)
                _enrollmentUiState.value = EnrollmentUiState.Error(
                    "Error al enviar la inscripción: ${e.message ?: "Error desconocido"}"
                )
            }
        }
    }

    /**
     * Reinicia el formulario a su estado inicial
     */
    fun resetForm() {
        _formData.value = EnrollmentFormData()
        _currentStep.value = 0
        _enrollmentUiState.value = EnrollmentUiState.Editing
    }

    /**
     * Limpia el estado de error/éxito y vuelve a Editing
     */
    fun clearEnrollmentState() {
        _enrollmentUiState.value = EnrollmentUiState.Editing
    }

    // ========================================================================
    // FUNCIONES AUXILIARES
    // ========================================================================

    /**
     * Muestra un mensaje de error temporalmente
     */
    private fun showError(message: String) {
        _enrollmentUiState.value = EnrollmentUiState.Error(message)

        // Auto-limpiar el error después de 3 segundos
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            if (_enrollmentUiState.value is EnrollmentUiState.Error) {
                _enrollmentUiState.value = EnrollmentUiState.Editing
            }
        }
    }
}

// ============================================================================
// DATA CLASSES Y SEALED CLASSES
// ============================================================================

/**
 * Modelo de curso (funcionalidad existente)
 */
data class Course(
    val name: String,
    val grade: String,
    val teacher: String
)

/**
 * Estados posibles de la UI de inscripción
 */
sealed class EnrollmentUiState {
    object Editing : EnrollmentUiState()
    object Loading : EnrollmentUiState()
    data class Error(val message: String) : EnrollmentUiState()
    data class Success(val message: String) : EnrollmentUiState()
}

/**
 * Datos del formulario de inscripción
 */
data class EnrollmentFormData(
    // Paso 1: Datos personales
    val nombre: String = "",
    val apellido: String = "",
    val documentoIdentidad: String = "",
    val fechaNacimiento: String = "", // Formato: DD/MM/YYYY
    val telefono: String = "",
    val email: String = "",

    // Paso 2: Documentos
    val dniDocumento: Uri? = null,
    val actaNacimiento: Uri? = null,
    val certificadoAcademico: Uri? = null
)

/**
 * Resultado de validación
 */
data class ValidationResult(
    val isValid: Boolean,
    val message: String
)