package com.mora.matritech.screens.teaching

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TeacherScreen(viewModel: TeacherViewModel) {

    val subjects = viewModel.subjects.collectAsState()

    // Cargar datos automáticamente al entrar
    LaunchedEffect(Unit) {
        viewModel.loadSubjects()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Panel del Docente",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(subjects.value) { subject ->
                SubjectCard(subject)
            }
        }
    }
}

@Composable
fun SubjectCard(subject: Subject) {
    Card(
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Materia: ${subject.name}",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Grupo: ${subject.grade}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Estudiantes: ${subject.students}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

