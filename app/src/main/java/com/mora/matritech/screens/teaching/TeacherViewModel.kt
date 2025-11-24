package com.mora.matritech.screens.teaching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TeacherViewModel : ViewModel() {

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects

    fun loadSubjects() {
        viewModelScope.launch {

            // Datos de prueba por ahora
            _subjects.value = listOf(
                Subject(name = "Matemáticas", grade = "1ro A", students = 28),
                Subject(name = "Física", grade = "2do B", students = 31),
                Subject(name = "Geometría", grade = "3ro A", students = 26)
            )
        }
    }
}

data class Subject(
    val name: String,
    val grade: String,
    val students: Int
)
