// StudentViewModel.kt
package com.mora.matritech.screens.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudentViewModel : ViewModel() {

    private val _courses: MutableStateFlow<List<Course>> = MutableStateFlow(emptyList())
    val courses: StateFlow<List<Course>> = _courses

    fun loadCourses() {
        viewModelScope.launch {
            // Datos de prueba por ahora
            _courses.value = listOf(
                Course(name = "Matemáticas", grade = "A", teacher = "Juan Pérez"),
                Course(name = "Historia", grade = "B", teacher = "María Gómez"),
                Course(name = "Ciencias", grade = "A", teacher = "Carlos López")
            )
        }
    }
}


data class Course(
    val name: String,
    val grade: String,
    val teacher: String
)