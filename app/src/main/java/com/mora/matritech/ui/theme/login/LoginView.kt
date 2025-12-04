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
            _uiState.value = LoginUiState(isLoading = true)

            when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> {
                    val user = result.user
                    val roleId = user?.rol_id

                    // Mapear rol_id a UserRole y string para guardar en sesión
                    val (userRole, roleString) = when (roleId) {
                        0 -> UserRole.SUPER_ADMIN to "superadmin"
                        1 -> UserRole.ADMIN to "admin"
                        2 -> UserRole.COORDINATOR to "coordinador"
                        3 -> UserRole.STUDENT to "estudiante"
                        4 -> UserRole.TEACHER to "docente"
                        5 -> UserRole.REPRESENTANTE to "representante"
                        else -> null to ""
                    }

                    // Guardar sesión con el rol en minúsculas
                    sessionManager.saveSession(
                        userId = user?.id,
                        role = roleString
                    )

                    _uiState.value = LoginUiState(
                        isLoading = false,
                        isSuccess = true,
                        isLoggedIn = true,
                        userRole = userRole
                    )
                }

                is AuthResult.Error -> {
                    _uiState.value = LoginUiState(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }

                AuthResult.Loading -> {
                    _uiState.value = LoginUiState(isLoading = true)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}