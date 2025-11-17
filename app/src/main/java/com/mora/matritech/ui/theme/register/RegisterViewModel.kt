package com.mora.matritech.ui.theme.Register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mora.matritech.data.repository.AuthRepository
import com.mora.matritech.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, confirmPassword: String, nombre: String) {
        viewModelScope.launch {
            println("🔵 INICIO REGISTRO")
            println("📧 Email: $email")
            println("👤 Nombre: $nombre")

            // Validaciones
            if (email.isBlank() || password.isBlank() || nombre.isBlank()) {
                println("❌ Campos vacíos")
                _uiState.value = RegisterUiState(
                    errorMessage = "Por favor completa todos los campos"
                )
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                println("❌ Email inválido")
                _uiState.value = RegisterUiState(
                    errorMessage = "Correo electrónico inválido"
                )
                return@launch
            }

            if (password.length < 6) {
                println("❌ Contraseña muy corta")
                _uiState.value = RegisterUiState(
                    errorMessage = "La contraseña debe tener al menos 6 caracteres"
                )
                return@launch
            }

            if (password != confirmPassword) {
                println("❌ Contraseñas no coinciden")
                _uiState.value = RegisterUiState(
                    errorMessage = "Las contraseñas no coinciden"
                )
                return@launch
            }

            println("✅ Validaciones pasadas, iniciando registro...")

            // Mostrar loading
            _uiState.value = RegisterUiState(isLoading = true)

            // Intentar registro
            println("🔄 Llamando a authRepository.signUp...")
            when (val result = authRepository.signUp(email, password, nombre)) {
                is AuthResult.Success -> {
                    println("✅ Registro exitoso!")
                    _uiState.value = RegisterUiState(isSuccess = true)
                }
                is AuthResult.Error -> {
                    println("❌ Error en registro: ${result.message}")
                    _uiState.value = RegisterUiState(
                        errorMessage = result.message
                    )
                }
                else -> {
                    println("⚠️ Resultado desconocido")
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}