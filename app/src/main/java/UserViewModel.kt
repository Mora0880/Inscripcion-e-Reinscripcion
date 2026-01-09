package com.mora.matritech.screens.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mora.matritech.data.repository.UserRepository
import com.mora.matritech.data.repository.UserStats
import com.mora.matritech.model.UserData
import com.mora.matritech.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Estado de la UI para gestión de usuarios
 */
data class UserManagementUiState(
    val users: List<UserData> = emptyList(),
    val filteredUsers: List<UserData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedRoleFilter: Int? = null, // null = todos
    val showActiveOnly: Boolean = true,
    val searchQuery: String = "",
    val stats: UserStats? = null,
    val successMessage: String? = null
)

/**
 * Estado del formulario de usuario
 */
data class UserFormState(
    val isEditing: Boolean = false,
    val userId: String? = null,
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedRole: UserRole = UserRole.STUDENT,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la gestión de usuarios (Admin)
 */
class UserViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(UserFormState())
    val formState: StateFlow<UserFormState> = _formState.asStateFlow()

    init {
        loadUsers()
        loadStats()
    }

    // ==================== CARGAR DATOS ====================

    /**
     * Cargar todos los usuarios
     */
    fun loadUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            println("🔄 Cargando usuarios...")
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }

            val result = repository.getAllUsers(
                roleFilter = _uiState.value.selectedRoleFilter,
                activeOnly = if (_uiState.value.showActiveOnly) true else null
            )

            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { users ->
                        println("✅ ${users.size} usuarios cargados")
                        _uiState.value = _uiState.value.copy(
                            users = users,
                            filteredUsers = filterUsers(users),
                            isLoading = false
                        )
                    },
                    onFailure = { error ->
                        println("❌ Error al cargar usuarios: ${error.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error al cargar usuarios: ${error.message}"
                        )
                    }
                )
            }
        }
    }

    /**
     * Cargar estadísticas
     */
    private fun loadStats() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getUserStats().fold(
                onSuccess = { stats ->
                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(stats = stats)
                    }
                },
                onFailure = { /* No crítico */ }
            )
        }
    }

    // ==================== FILTROS Y BÚSQUEDA ====================

    /**
     * Filtrar por rol
     */
    fun filterByRole(roleId: Int?) {
        _uiState.value = _uiState.value.copy(selectedRoleFilter = roleId)
        loadUsers()
    }

    /**
     * Mostrar solo activos/todos
     */
    fun toggleActiveFilter() {
        _uiState.value = _uiState.value.copy(
            showActiveOnly = !_uiState.value.showActiveOnly
        )
        loadUsers()
    }

    /**
     * Buscar usuarios
     */
    fun searchUsers(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                filteredUsers = _uiState.value.users
            )
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.searchUsers(query)
            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { users ->
                        _uiState.value = _uiState.value.copy(
                            filteredUsers = filterUsers(users)
                        )
                    },
                    onFailure = { /* Mantener lista actual */ }
                )
            }
        }
    }

    /**
     * Aplicar filtros a la lista de usuarios
     */
    private fun filterUsers(users: List<UserData>): List<UserData> {
        var filtered = users

        // Filtro por rol
        _uiState.value.selectedRoleFilter?.let { roleId ->
            filtered = filtered.filter { it.roleId == roleId }
        }

        // Filtro por estado
        if (_uiState.value.showActiveOnly) {
            filtered = filtered.filter { it.activo }
        }

        return filtered
    }

    // ==================== CREAR USUARIO ====================

    /**
     * Preparar formulario para crear usuario
     */
    fun prepareCreateUser() {
        _formState.value = UserFormState(isEditing = false)
    }

    /**
     * Crear nuevo usuario
     */
    fun createUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _formState.value

            // Validaciones
            if (!validateForm()) return@launch

            println("➕ Creando usuario: ${state.email}")
            withContext(Dispatchers.Main) {
                _formState.value = state.copy(isLoading = true, error = null)
            }

            val result = repository.createUser(
                email = state.email,
                password = state.password,
                nombre = state.nombre,
                roleId = state.selectedRole.id
            )

            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { user ->
                        println("✅ Usuario creado: ${user.email}")
                        _formState.value = UserFormState() // Limpiar formulario
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Usuario creado exitosamente"
                        )
                        loadUsers()
                        loadStats()
                    },
                    onFailure = { error ->
                        println("❌ Error: ${error.message}")
                        _formState.value = state.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            }
        }
    }

    // ==================== EDITAR USUARIO ====================

    /**
     * Preparar formulario para editar usuario
     */
    fun prepareEditUser(user: UserData) {
        _formState.value = UserFormState(
            isEditing = true,
            userId = user.id,
            nombre = user.nombre,
            email = user.email,
            selectedRole = UserRole.fromId(user.roleId) ?: UserRole.STUDENT
        )
    }

    /**
     * Actualizar usuario existente
     */
    fun updateUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _formState.value

            if (state.userId == null) {
                withContext(Dispatchers.Main) {
                    _formState.value = state.copy(error = "ID de usuario no válido")
                }
                return@launch
            }

            // Validaciones
            if (state.nombre.isBlank() || state.email.isBlank()) {
                withContext(Dispatchers.Main) {
                    _formState.value = state.copy(error = "Completa todos los campos")
                }
                return@launch
            }

            println("✏️ Actualizando usuario: ${state.userId}")
            withContext(Dispatchers.Main) {
                _formState.value = state.copy(isLoading = true, error = null)
            }

            val result = repository.updateUser(
                userId = state.userId,
                nombre = state.nombre,
                email = state.email,
                roleId = state.selectedRole.id
            )

            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { user ->
                        println("✅ Usuario actualizado: ${user.email}")
                        _formState.value = UserFormState()
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Usuario actualizado exitosamente"
                        )
                        loadUsers()
                    },
                    onFailure = { error ->
                        println("❌ Error: ${error.message}")
                        _formState.value = state.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            }
        }
    }

    // ==================== ACTIVAR/DESACTIVAR ====================

    /**
     * Desactivar usuario (soft delete)
     */
    fun deactivateUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            println("🚫 Desactivando usuario: $userId")

            val result = repository.deactivateUser(userId)
            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Usuario desactivado"
                        )
                        loadUsers()
                        loadStats()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Error al desactivar: ${error.message}"
                        )
                    }
                )
            }
        }
    }

    /**
     * Activar usuario
     */
    fun activateUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            println("✅ Activando usuario: $userId")

            val result = repository.activateUser(userId)
            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Usuario activado"
                        )
                        loadUsers()
                        loadStats()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Error al activar: ${error.message}"
                        )
                    }
                )
            }
        }
    }

    // ==================== FORMULARIO ====================

    fun updateFormNombre(nombre: String) {
        _formState.value = _formState.value.copy(nombre = nombre)
    }

    fun updateFormEmail(email: String) {
        _formState.value = _formState.value.copy(email = email)
    }

    fun updateFormPassword(password: String) {
        _formState.value = _formState.value.copy(password = password)
    }

    fun updateFormConfirmPassword(confirmPassword: String) {
        _formState.value = _formState.value.copy(confirmPassword = confirmPassword)
    }

    fun updateFormRole(role: UserRole) {
        _formState.value = _formState.value.copy(selectedRole = role)
    }

    /**
     * Validar formulario
     */
    private suspend fun validateForm(): Boolean {
        val state = _formState.value

        when {
            state.nombre.isBlank() -> {
                withContext(Dispatchers.Main) {
                    _formState.value = state.copy(error = "El nombre es requerido")
                }
                return false
            }
            state.email.isBlank() -> {
                withContext(Dispatchers.Main) {
                    _formState.value = state.copy(error = "El email es requerido")
                }
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> {
                withContext(Dispatchers.Main) {
                    _formState.value = state.copy(error = "Email inválido")
                }
                return false
            }
            !state.isEditing && state.password.length < 6 -> {
                withContext(Dispatchers.Main) {
                    _formState.value = state.copy(
                        error = "La contraseña debe tener al menos 6 caracteres"
                    )
                }
                return false
            }
            !state.isEditing && state.password != state.confirmPassword -> {
                withContext(Dispatchers.Main) {
                    _formState.value = state.copy(error = "Las contraseñas no coinciden")
                }
                return false
            }
        }

        return true
    }

    // ==================== MENSAJES ====================

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearFormError() {
        _formState.value = _formState.value.copy(error = null)
    }

    fun cancelForm() {
        _formState.value = UserFormState()
    }
}