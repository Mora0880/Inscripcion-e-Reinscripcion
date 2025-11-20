package com.mora.matritech.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Institucion(
    @SerialName("id")
    val id: String? = null,

    @SerialName("nombre_institucion")
    val nombre: String = "",

    @SerialName("provincia")
    val provincia: String? = null,

    @SerialName("direccion_especifica")
    val direccion: String? = null,

    @SerialName("codigo_identificacion")
    val codigoIdentificacion: String? = null,

    @SerialName("ano_laboracion")
    val anoLaboracion: Int? = null,

    @SerialName("tipo_institucion")
    val tipoInstitucion: String? = null,

    @SerialName("nivel_educativo")
    val nivelEducativo: String? = null,

    @SerialName("regimen")
    val regimen: String? = null,

    @SerialName("contacto")
    val contacto: String? = null,

    @SerialName("created_at")
    val fechaCreacion: String? = null
)

// Estado UI para la pantalla de instituciones
data class InstitucionesUiState(
    val instituciones: List<Institucion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDialog: Boolean = false,
    val institucionSeleccionada: Institucion? = null
)

// Eventos de la UI
sealed class InstitucionEvent {
    data class LoadInstituciones(val forceRefresh: Boolean = false) : InstitucionEvent()
    data class CreateInstitucion(val institucion: Institucion) : InstitucionEvent()
    data class UpdateInstitucion(val institucion: Institucion) : InstitucionEvent()
    data class DeleteInstitucion(val id: String) : InstitucionEvent()
    data class SelectInstitucion(val institucion: Institucion?) : InstitucionEvent()
    object ShowDialog : InstitucionEvent()
    object HideDialog : InstitucionEvent()
    object ClearError : InstitucionEvent()
}