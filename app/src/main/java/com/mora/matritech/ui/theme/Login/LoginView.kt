package com.mora.matritech.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mora.matritech.data.repository.AuthRepository
import com.mora.matritech.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Validaciones
            if (email.isBlank() || password.isBlank()) {
                _uiState.value = LoginUiState(
                    errorMessage = "Por favor completa todos los campos"
                )
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _uiState.value = LoginUiState(
                    errorMessage = "Correo electrónico inválido"
                )
                return@launch
            }

            // Mostrar loading
            _uiState.value = LoginUiState(isLoading = true)

            // Intentar login
            when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = LoginUiState(isSuccess = true)
                }
                is AuthResult.Error -> {
                    _uiState.value = LoginUiState(
                        errorMessage = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}