package com.mora.matritech.screens.coordinator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CoordinatorViewModel : ViewModel() {

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups

    fun loadGroups() {
        viewModelScope.launch {

            // Datos de prueba por ahora
            _groups.value = listOf(
                Group(grade = "1ro", section = "A", enrolled = 28),
                Group(grade = "2do", section = "B", enrolled = 31),
                Group(grade = "3ro", section = "A", enrolled = 26)
            )
        }
    }
}

data class Group(
    val grade: String,
    val section: String,
    val enrolled: Int
)
