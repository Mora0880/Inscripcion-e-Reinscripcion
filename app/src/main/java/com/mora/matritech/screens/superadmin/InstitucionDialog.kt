package com.mora.matritech.screens.superadmin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mora.matritech.model.Institucion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstitucionDialog(
    institucion: Institucion?,
    onDismiss: () -> Unit,
    onSave: (Institucion) -> Unit
) {
    var nombre by remember { mutableStateOf(institucion?.nombre ?: "") }
    var provincia by remember { mutableStateOf(institucion?.provincia ?: "") }
    var direccion by remember { mutableStateOf(institucion?.direccion ?: "") }
    var codigoIdentificacion by remember { mutableStateOf(institucion?.codigoIdentificacion ?: "") }
    var anoLaboracion by remember { mutableStateOf(institucion?.anoLaboracion?.toString() ?: "") }
    var tipoInstitucion by remember { mutableStateOf(institucion?.tipoInstitucion ?: "") }
    var nivelEducativo by remember { mutableStateOf(institucion?.nivelEducativo ?: "") }
    var regimen by remember { mutableStateOf(institucion?.regimen ?: "") }
    var contacto by remember { mutableStateOf(institucion?.contacto ?: "") }

    var nombreError by remember { mutableStateOf(false) }
    var anoError by remember { mutableStateOf(false) }

    fun validateFields(): Boolean {
        nombreError = nombre.isBlank()
        anoError = anoLaboracion.isNotBlank() && anoLaboracion.toIntOrNull() == null
        return !nombreError && !anoError
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (institucion == null) "Nueva Institución" else "Editar Institución"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        nombreError = false
                    },
                    label = { Text("Nombre de la Institución *") },
                    isError = nombreError,
                    supportingText = if (nombreError) {
                        { Text("El nombre es obligatorio") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Provincia
                OutlinedTextField(
                    value = provincia,
                    onValueChange = { provincia = it },
                    label = { Text("Provincia") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Dirección
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección Específica") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                // Código de Identificación
                OutlinedTextField(
                    value = codigoIdentificacion,
                    onValueChange = { codigoIdentificacion = it },
                    label = { Text("Código de Identificación") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Año de Laboración
                OutlinedTextField(
                    value = anoLaboracion,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            anoLaboracion = it
                            anoError = false
                        }
                    },
                    label = { Text("Año de Laboración") },
                    isError = anoError,
                    supportingText = if (anoError) {
                        { Text("Debe ser un número válido") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Tipo de Institución
                OutlinedTextField(
                    value = tipoInstitucion,
                    onValueChange = { tipoInstitucion = it },
                    label = { Text("Tipo de Institución") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Nivel Educativo
                OutlinedTextField(
                    value = nivelEducativo,
                    onValueChange = { nivelEducativo = it },
                    label = { Text("Nivel Educativo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Régimen
                OutlinedTextField(
                    value = regimen,
                    onValueChange = { regimen = it },
                    label = { Text("Régimen") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Contacto
                OutlinedTextField(
                    value = contacto,
                    onValueChange = { contacto = it },
                    label = { Text("Contacto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "* Campos obligatorios",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validateFields()) {
                        val nuevaInstitucion = Institucion(
                            id = institucion?.id ?: "",
                            nombre = nombre.trim(),
                            provincia = provincia.trim().ifBlank { null },
                            direccion = direccion.trim().ifBlank { null },
                            codigoIdentificacion = codigoIdentificacion.trim().ifBlank { null },
                            anoLaboracion = anoLaboracion.toIntOrNull(),
                            tipoInstitucion = tipoInstitucion.trim().ifBlank { null },
                            nivelEducativo = nivelEducativo.trim().ifBlank { null },
                            regimen = regimen.trim().ifBlank { null },
                            contacto = contacto.trim().ifBlank { null },
                            fechaCreacion = institucion?.fechaCreacion
                        )
                        onSave(nuevaInstitucion)
                    }
                }
            ) {
                Text(if (institucion == null) "Crear" else "Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}