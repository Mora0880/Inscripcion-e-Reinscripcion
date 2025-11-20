package com.mora.matritech.screens.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class AdminStats(
    val totalUsers: Int = 48,
    val students: Int = 32,
    val teachers: Int = 12,
    val admins: Int = 4
)

data class AdminUiState(
    val selectedBottomItem: String = "home",
    val stats: AdminStats = AdminStats(),
    val isDrawerOpen: Boolean = false
)

class AdminViewModel : ViewModel() {
    private val _uiState = mutableStateOf(AdminUiState())
    val uiState: State<AdminUiState> = _uiState

    fun onBottomItemSelected(item: String) {
        _uiState.value = _uiState.value.copy(selectedBottomItem = item)
    }

    fun openDrawer() {
        _uiState.value = _uiState.value.copy(isDrawerOpen = true)
    }

    fun closeDrawer() {
        _uiState.value = _uiState.value.copy(isDrawerOpen = false)
    }

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            // Llamadas reales más adelante
        }
    }
}
