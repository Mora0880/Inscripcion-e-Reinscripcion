package com.mora.matritech.screens.superadmin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mora.matritech.data.remote.supabase
import com.mora.matritech.model.Institucion
import com.mora.matritech.model.InstitucionEvent
import com.mora.matritech.model.InstitucionesUiState
import com.mora.matritech.data.repository.InstitucionRepository
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InstitucionViewModel(
    private val repository: InstitucionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InstitucionesUiState())
    val uiState: StateFlow<InstitucionesUiState> = _uiState.asStateFlow()

    init {
        loadInstituciones()
    }

    fun onEvent(event: InstitucionEvent) {
        when (event) {
            is InstitucionEvent.LoadInstituciones -> loadInstituciones(event.forceRefresh)
            is InstitucionEvent.CreateInstitucion -> createInstitucion(event.institucion)
            is InstitucionEvent.UpdateInstitucion -> updateInstitucion(event.institucion)
            is InstitucionEvent.DeleteInstitucion -> deleteInstitucion(event.id)
            is InstitucionEvent.SelectInstitucion -> selectInstitucion(event.institucion)
            is InstitucionEvent.ShowDialog -> showDialog()
            is InstitucionEvent.HideDialog -> hideDialog()
            is InstitucionEvent.ClearError -> clearError()
        }
    }

    private fun loadInstituciones(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getAllInstituciones()
                .onSuccess { instituciones ->
                    _uiState.update {
                        it.copy(
                            instituciones = instituciones,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar instituciones: ${error.message}"
                        )
                    }
                }
        }
    }

    fun createInstitucion(institucion: Institucion) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.createInstitucion(institucion.copy(id = null))  // ← fuerza null
                .onSuccess {
                    loadInstituciones()
                    hideDialog()
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al crear: ${error.message}"
                        )
                    }
                }
        }
    }

    private fun updateInstitucion(institucion: Institucion) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.updateInstitucion(institucion)
                .onSuccess {
                    loadInstituciones()
                    hideDialog()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al actualizar institución: ${error.message}"
                        )
                    }
                }
        }
    }

    private fun deleteInstitucion(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.deleteInstitucion(id)
                .onSuccess {
                    loadInstituciones()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al eliminar institución: ${error.message}"
                        )
                    }
                }
        }
    }

    private fun selectInstitucion(institucion: Institucion?) {
        _uiState.update {
            it.copy(
                institucionSeleccionada = institucion,
                showDialog = institucion != null
            )
        }
    }

    private fun showDialog() {
        _uiState.update { it.copy(showDialog = true, institucionSeleccionada = null) }
    }

    private fun hideDialog() {
        _uiState.update {
            it.copy(
                showDialog = false,
                institucionSeleccionada = null
            )
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}