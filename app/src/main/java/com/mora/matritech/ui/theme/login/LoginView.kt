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
                    val userRole = when (roleId) {
                        1 -> UserRole.ADMIN
                        2 -> UserRole.COORDINATOR
                        3 -> UserRole.STUDENT
                        4 -> UserRole.TEACHER
                        5 -> UserRole.REPRESENTANTE
                        else -> null
                    }

                    // ✅ GUARDAR SESIÓN usando tu SessionManager
                    sessionManager.saveSession(
                        userId = user?.id,
                        role = userRole?.name ?: ""
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

                AuthResult.Loading -> TODO()
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}