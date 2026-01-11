package com.mora.matritech.screens.admin

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mora.matritech.data.repository.EnrollmentRepository
import com.mora.matritech.data.repository.EnrollmentResult
import com.mora.matritech.data.repository.Inscripcion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar inscripciones desde el panel de administración
 */
class AdminEnrollmentsViewModel(
    context: Context,
    private val adminId: String
) : ViewModel() {

    private val repository = EnrollmentRepository(context)

    companion object {
        private const val TAG = "AdminEnrollmentsVM"
    }

    // Estado de la UI
    private val _uiState = MutableStateFlow<EnrollmentsUiState>(EnrollmentsUiState.Loading)
    val uiState: StateFlow<EnrollmentsUiState> = _uiState.asStateFlow()

    // Lista de inscripciones visibles (filtradas)
    private val _enrollments = MutableStateFlow<List<Inscripcion>>(emptyList())
    val enrollments: StateFlow<List<Inscripcion>> = _enrollments.asStateFlow()

    // Filtro actual - Cambiado a "todas" para ver aprobadas/rechazadas
    private val _filterState = MutableStateFlow("todas")
    val filterState: StateFlow<String> = _filterState.asStateFlow()

    // Diálogo de acción (aprobar/rechazar)
    private val _actionDialogState = MutableStateFlow<ActionDialogState>(ActionDialogState.Hidden)
    val actionDialogState: StateFlow<ActionDialogState> = _actionDialogState.asStateFlow()

    init {
        Log.d(TAG, "🚀 Inicializando ViewModel con adminId: $adminId")
        loadEnrollments()
    }

    /**
     * Carga TODAS las inscripciones y aplica filtro en cliente
     */
    fun loadEnrollments(delayMs: Long = 0) {
        viewModelScope.launch {
            try {
                _uiState.value = EnrollmentsUiState.Loading
                Log.d(TAG, "🔄 Cargando inscripciones (filtro: ${_filterState.value})")

                // Limpiamos lista anterior
                _enrollments.value = emptyList()

                // Delay opcional para dar tiempo a que Supabase actualice
                if (delayMs > 0) {
                    kotlinx.coroutines.delay(delayMs)
                    Log.d(TAG, "⏱️ Esperando ${delayMs}ms para consistencia de datos...")
                }

                // Traemos TODAS las inscripciones
                val allEnrollments = repository.getAllEnrollments()
                Log.d(TAG, "📦 Total inscripciones obtenidas: ${allEnrollments.size}")

                // Log de estados para debugging
                allEnrollments.groupBy { it.estado }.forEach { (estado, lista) ->
                    Log.d(TAG, "   └─ $estado: ${lista.size}")
                }

                // Aplicamos filtro
                val filtered = when (_filterState.value.lowercase()) {
                    "todas" -> allEnrollments
                    "pendiente" -> allEnrollments.filter { it.estado == "pendiente" }
                    "aprobada" -> allEnrollments.filter { it.estado == "aprobada" }
                    "rechazada" -> allEnrollments.filter { it.estado == "rechazada" }
                    else -> allEnrollments
                }

                _enrollments.value = filtered
                _uiState.value = EnrollmentsUiState.Success

                Log.d(TAG, "✅ Mostrando ${filtered.size} inscripciones filtradas")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando inscripciones", e)
                _uiState.value = EnrollmentsUiState.Error(
                    e.message ?: "Error al cargar inscripciones"
                )
            }
        }
    }

    fun setFilter(filter: String) {
        Log.d(TAG, "🔍 Cambiando filtro a: $filter")
        _filterState.value = filter
        loadEnrollments()
    }

    fun showApproveDialog(inscripcion: Inscripcion) {
        Log.d(TAG, "💬 Mostrando diálogo de aprobación para: ${inscripcion.id}")
        _actionDialogState.value = ActionDialogState.Approve(inscripcion)
    }

    fun showRejectDialog(inscripcion: Inscripcion) {
        Log.d(TAG, "💬 Mostrando diálogo de rechazo para: ${inscripcion.id}")
        _actionDialogState.value = ActionDialogState.Reject(inscripcion)
    }

    fun hideActionDialog() {
        Log.d(TAG, "🚪 Ocultando diálogo")
        _actionDialogState.value = ActionDialogState.Hidden
    }

    fun approveEnrollment(inscripcionId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🟢 Iniciando aprobación de inscripción: $inscripcionId")
                _uiState.value = EnrollmentsUiState.Loading

                val result = repository.approveEnrollment(inscripcionId, adminId)

                when (result) {
                    is EnrollmentResult.Success -> {
                        Log.d(TAG, "✅ Aprobación exitosa")
                        hideActionDialog()
                        // Cambiar a "todas" para mostrar la inscripción aprobada
                        _filterState.value = "todas"
                        // Agregamos un delay de 500ms para dar tiempo a que Supabase confirme el cambio
                        loadEnrollments(delayMs = 500)
                        _uiState.value = EnrollmentsUiState.ActionSuccess("✅ Inscripción aprobada exitosamente")
                    }
                    is EnrollmentResult.Error -> {
                        Log.e(TAG, "❌ Error en aprobación: ${result.message}")
                        _uiState.value = EnrollmentsUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción en approveEnrollment", e)
                _uiState.value = EnrollmentsUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun rejectEnrollment(inscripcionId: String, motivo: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔴 Iniciando rechazo de inscripción: $inscripcionId")
                Log.d(TAG, "📝 Motivo: $motivo")
                _uiState.value = EnrollmentsUiState.Loading

                val result = repository.rejectEnrollment(inscripcionId, adminId, motivo)

                when (result) {
                    is EnrollmentResult.Success -> {
                        Log.d(TAG, "✅ Rechazo exitoso")
                        hideActionDialog()
                        // Cambiar a "todas" para mostrar la inscripción rechazada
                        _filterState.value = "todas"
                        // Agregamos un delay de 500ms para dar tiempo a que Supabase confirme el cambio
                        loadEnrollments(delayMs = 500)
                        _uiState.value = EnrollmentsUiState.ActionSuccess("✅ Inscripción rechazada")
                    }
                    is EnrollmentResult.Error -> {
                        Log.e(TAG, "❌ Error en rechazo: ${result.message}")
                        _uiState.value = EnrollmentsUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción en rejectEnrollment", e)
                _uiState.value = EnrollmentsUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun clearMessage() {
        if (_uiState.value is EnrollmentsUiState.ActionSuccess ||
            _uiState.value is EnrollmentsUiState.Error) {
            _uiState.value = EnrollmentsUiState.Success
        }
    }

    fun getEnrollmentStats(): EnrollmentStats {
        val list = _enrollments.value
        return EnrollmentStats(
            total = list.size,
            pendientes = list.count { it.estado == "pendiente" },
            aprobadas = list.count { it.estado == "aprobada" },
            rechazadas = list.count { it.estado == "rechazada" }
        )
    }
}

// ============================ ESTADOS ============================

sealed class EnrollmentsUiState {
    object Loading : EnrollmentsUiState()
    object Success : EnrollmentsUiState()
    data class Error(val message: String) : EnrollmentsUiState()
    data class ActionSuccess(val message: String) : EnrollmentsUiState()
}

sealed class ActionDialogState {
    object Hidden : ActionDialogState()
    data class Approve(val inscripcion: Inscripcion) : ActionDialogState()
    data class Reject(val inscripcion: Inscripcion) : ActionDialogState()
}

data class EnrollmentStats(
    val total: Int = 0,
    val pendientes: Int = 0,
    val aprobadas: Int = 0,
    val rechazadas: Int = 0
)