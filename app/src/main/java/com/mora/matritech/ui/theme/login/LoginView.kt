package com.mora.matritech.ui.theme.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mora.matritech.data.local.SessionManager
import com.mora.matritech.data.repository.AuthRepository
import com.mora.matritech.data.repository.AuthResult
import com.mora.matritech.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userRole: UserRole? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            println("🔵 INICIANDO LOGIN")
            println("📧 Email: $email")

            _uiState.value = LoginUiState(isLoading = true)

            when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> {
                    println("✅ AuthResult.Success recibido")

                    val user = result.user
                    val roleId = user?.rol_id

                    println("👤 Usuario obtenido: ${user?.email}")
                    println("🎭 Rol ID: $roleId")

                    // Validar que tenemos usuario y rol
                    if (user == null || user.id.isNullOrEmpty()) {
                        println("❌ Error: Usuario nulo o sin ID")
                        _uiState.value = LoginUiState(
                            isLoading = false,
                            errorMessage = "Error al obtener datos del usuario"
                        )
                        return@launch
                    }

                    if (roleId == null) {
                        println("❌ Error: Usuario sin rol asignado")
                        _uiState.value = LoginUiState(
                            isLoading = false,
                            errorMessage = "Usuario sin rol asignado. Contacta al administrador."
                        )
                        return@launch
                    }

                    // Mapear rol_id a UserRole y string para guardar en sesión
                    val (userRole, roleString) = when (roleId) {
                        0 -> UserRole.SUPER_ADMIN to "superadmin"
                        1 -> UserRole.ADMIN to "admin"
                        2 -> UserRole.COORDINATOR to "coordinador"
                        3 -> UserRole.STUDENT to "estudiante"
                        4 -> UserRole.TEACHER to "docente"
                        5 -> UserRole.REPRESENTANTE to "representante"
                        else -> {
                            println("❌ Rol ID desconocido: $roleId")
                            null to ""
                        }
                    }

                    if (userRole == null || roleString.isEmpty()) {
                        println("❌ Error: Rol inválido")
                        _uiState.value = LoginUiState(
                            isLoading = false,
                            errorMessage = "Rol de usuario inválido"
                        )
                        return@launch
                    }

                    println("🎭 Rol mapeado: $roleString")

                    // Guardar sesión
                    sessionManager.saveSession(user.id, roleString)

                    // Verificar que se guardó
                    val savedRole = sessionManager.getUserRole()
                    val isLogged = sessionManager.isLoggedIn()
                    println("✅ Sesión verificada - Logueado: $isLogged, Rol guardado: $savedRole")

                    _uiState.value = LoginUiState(
                        isLoading = false,
                        isSuccess = true,
                        isLoggedIn = true,
                        userRole = userRole
                    )

                    println("🟢 LOGIN COMPLETADO EXITOSAMENTE")
                }

                is AuthResult.Error -> {
                    println("❌ Error en login: ${result.message}")
                    _uiState.value = LoginUiState(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }

                AuthResult.Loading -> {
                    println("⏳ Loading...")
                    _uiState.value = LoginUiState(isLoading = true)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}