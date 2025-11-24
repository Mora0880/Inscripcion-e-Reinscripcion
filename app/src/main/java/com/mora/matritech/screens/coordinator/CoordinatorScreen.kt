package com.mora.matritech.screens.coordinator

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
import androidx.compose.ui.unit.dp


@Composable
fun CoordinatorScreen(viewModel: CoordinatorViewModel) {

    val groups = viewModel.groups.collectAsState()

    // Cargar lista cuando entra a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Panel del Coordinador",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(groups.value) { group ->
                GroupCard(group)
            }
        }
    }
}

@Composable
fun GroupCard(group: Group) {
    Card(
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Grado: ${group.grade}",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Sección: ${group.section}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Estudiantes inscritos: ${group.enrolled}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
