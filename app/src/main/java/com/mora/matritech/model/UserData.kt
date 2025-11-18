package com.mora.matritech.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo para los datos del usuario en la tabla 'usuarios'
 */
@Serializable
data class UserData(
    val id: String,
    val nombre: String,
    val email: String,
    @SerialName("rol_id")
    val roleId: Int? = null,
    @SerialName("institucion_id")
    val institucionId: String? = null,
    @SerialName("es_super_admin")
    val esSuperAdmin: Boolean = false,
    @SerialName("fecha_creacion")
    val fechaCreacion: String? = null,
    val activo: Boolean = true
)

/**
 * Modelo para crear un nuevo usuario (sin ID, se genera automáticamente)
 */
@Serializable
data class CreateUserData(
    val id: String,
    val nombre: String,
    val email: String,
    @SerialName("rol_id")
    val roleId: Int,
    val activo: Boolean = true
)