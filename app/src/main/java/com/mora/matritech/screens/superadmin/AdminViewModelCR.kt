package com.mora.matritech.ui.screens.superadmin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mora.matritech.data.repository.UserRepository
import com.mora.matritech.data.repository.InstitucionRepository
import com.mora.matritech.model.UserData
import com.mora.matritech.model.Institucion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión de Administradores en SuperAdmin
 */
class AdminViewModelCR(
    private val userRepository: UserRepository,
    private val institucionRepository: InstitucionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadAdministradores()
        loadInstituciones()
    }

    fun onEvent(event: AdminEvent) {
        when (event) {
            is AdminEvent.LoadAdministradores -> loadAdministradores()
            is AdminEvent.SearchAdministradores -> searchAdministradores(event.query)
            is AdminEvent.CreateAdministrador -> createAdministrador(event.admin)
            is AdminEvent.UpdateAdministrador -> updateAdministrador(event.admin)
            is AdminEvent.DeleteAdministrador -> deleteAdministrador(event.adminId)
            is AdminEvent.ToggleActivoAdministrador -> toggleActivo(event.adminId, event.activo)
            is AdminEvent.SelectAdministrador -> selectAdministrador(event.admin)
            AdminEvent.ShowCreateDialog -> showCreateDialog()
            AdminEvent.ShowEditDialog -> showEditDialog()
            AdminEvent.HideDialog -> hideDialog()
            AdminEvent.ClearError -> clearError()
            AdminEvent.ClearSuccess -> clearSuccess()
        }
    }

    private fun loadAdministradores() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            userRepository.getAllUsers(
                roleFilter = 1, // Rol Admin = 1
                activeOnly = null // Mostrar activos e inactivos
            ).fold(
                onSuccess = { admins ->
                    _uiState.update {
                        it.copy(
                            administradores = admins,
                            administradoresFiltrados = admins,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar administradores: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    private fun loadInstituciones() {
        viewModelScope.launch {
            institucionRepository.getAllInstituciones().fold(
                onSuccess = { instituciones ->
                    _uiState.update { it.copy(instituciones = instituciones) }
                },
                onFailure = { error ->
                    println("Error al cargar instituciones: ${error.message}")
                }
            )
        }
    }

    private fun searchAdministradores(query: String) {
        val admins = _uiState.value.administradores
        val filtrados = if (query.isBlank()) {
            admins
        } else {
            admins.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        it.email.contains(query, ignoreCase = true)
            }
        }
        _uiState.update { it.copy(administradoresFiltrados = filtrados, searchQuery = query) }
    }

    private fun createAdministrador(admin: AdminFormData) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Validaciones
            if (!validateAdminData(admin)) {
                return@launch
            }

            // Crear usuario con institución incluida
            userRepository.createUser(
                email = admin.email,
                password = admin.password,
                nombre = admin.nombre,
                roleId = 1, // Admin
                institucionId = if (admin.institucionId.isNotEmpty()) admin.institucionId else null
            ).fold(
                onSuccess = { createdUser ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showDialog = false,
                            successMessage = "Administrador creado exitosamente"
                        )
                    }
                    loadAdministradores()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al crear administrador: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    private fun updateAdministrador(admin: AdminFormData) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val adminId = _uiState.value.adminSeleccionado?.id ?: return@launch

            // Actualizar usuario con institución incluida
            userRepository.updateUser(
                userId = adminId,
                nombre = admin.nombre,
                email = admin.email,
                roleId = 1, // Mantener como Admin
                institucionId = if (admin.institucionId.isNotEmpty()) admin.institucionId else null
            ).fold(
                onSuccess = { updatedUser ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showDialog = false,
                            successMessage = "Administrador actualizado exitosamente"
                        )
                    }
                    loadAdministradores()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al actualizar administrador: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    private fun deleteAdministrador(adminId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            userRepository.deactivateUser(adminId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Administrador eliminado exitosamente"
                        )
                    }
                    loadAdministradores()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al eliminar administrador: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    private fun toggleActivo(adminId: String, activo: Boolean) {
        viewModelScope.launch {
            val result = if (activo) {
                userRepository.activateUser(adminId)
            } else {
                userRepository.deactivateUser(adminId)
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(successMessage = if (activo) "Administrador activado" else "Administrador desactivado")
                    }
                    loadAdministradores()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = "Error al cambiar estado: ${error.message}")
                    }
                }
            )
        }
    }

    private fun validateAdminData(admin: AdminFormData): Boolean {
        when {
            admin.nombre.isBlank() -> {
                _uiState.update { it.copy(error = "El nombre es obligatorio", isLoading = false) }
                return false
            }
            admin.email.isBlank() -> {
                _uiState.update { it.copy(error = "El email es obligatorio", isLoading = false) }
                return false
            }
            !admin.email.contains("@") -> {
                _uiState.update { it.copy(error = "Email inválido", isLoading = false) }
                return false
            }
            admin.password.length < 6 && _uiState.value.adminSeleccionado == null -> {
                _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres", isLoading = false) }
                return false
            }
        }
        return true
    }

    private fun selectAdministrador(admin: UserData?) {
        _uiState.update { it.copy(adminSeleccionado = admin) }
    }

    private fun showCreateDialog() {
        _uiState.update { it.copy(showDialog = true, adminSeleccionado = null, dialogMode = DialogMode.CREATE) }
    }

    private fun showEditDialog() {
        _uiState.update { it.copy(showDialog = true, dialogMode = DialogMode.EDIT) }
    }

    private fun hideDialog() {
        _uiState.update { it.copy(showDialog = false, adminSeleccionado = null, error = null) }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

/**
 * Estado UI para la pantalla de Administradores
 */
data class AdminUiState(
    val administradores: List<UserData> = emptyList(),
    val administradoresFiltrados: List<UserData> = emptyList(),
    val instituciones: List<Institucion> = emptyList(),
    val adminSeleccionado: UserData? = null,
    val isLoading: Boolean = false,
    val showDialog: Boolean = false,
    val dialogMode: DialogMode = DialogMode.CREATE,
    val searchQuery: String = "",
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * Modo del diálogo (Crear o Editar)
 */
enum class DialogMode {
    CREATE, EDIT
}

/**
 * Eventos de la UI
 */
sealed class AdminEvent {
    object LoadAdministradores : AdminEvent()
    data class SearchAdministradores(val query: String) : AdminEvent()
    data class CreateAdministrador(val admin: AdminFormData) : AdminEvent()
    data class UpdateAdministrador(val admin: AdminFormData) : AdminEvent()
    data class DeleteAdministrador(val adminId: String) : AdminEvent()
    data class ToggleActivoAdministrador(val adminId: String, val activo: Boolean) : AdminEvent()
    data class SelectAdministrador(val admin: UserData?) : AdminEvent()
    object ShowCreateDialog : AdminEvent()
    object ShowEditDialog : AdminEvent()
    object HideDialog : AdminEvent()
    object ClearError : AdminEvent()
    object ClearSuccess : AdminEvent()
}

/**
 * Data class para el formulario
 */
data class AdminFormData(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val institucionId: String = ""
)