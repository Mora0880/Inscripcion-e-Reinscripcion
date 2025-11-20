package com.mora.matritech.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {

            // Loading
            _uiState.value = LoginUiState(isLoading = true)

            when (val result = authRepository.signIn(email, password)) {

                is AuthResult.Success -> {
                    val user = result.user

                    // Leer rol del usuario
                    // Leer rol del usuario
                    val roleId = user?.rol_id
                    val userRole = UserRole.fromId(roleId)

                    // Actualizar el UI state
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

                else -> {}
            }
        }
    }



    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}