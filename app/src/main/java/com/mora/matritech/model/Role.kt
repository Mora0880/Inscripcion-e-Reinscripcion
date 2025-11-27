package com.mora.matritech.model

import kotlinx.serialization.Serializable

/**
 * Modelo para los roles de usuario
 */
@Serializable
data class Role(
    val id: Int,
    val nombre: String
)

/**
 * Enum para facilitar el manejo de roles en la app
 */
enum class UserRole(val id: Int, val roleName: String, val displayName: String) {
    SUPER_ADMIN(0, "super_admin", "Super Administrador"),
    ADMIN(1, "administrador", "Administrador"),
    COORDINATOR(2, "coordinador_academico", "Coordinador Académico"),
    STUDENT(3, "estudiante", "Estudiante"),
    TEACHER(4, "docente", "Docente"),
    REPRESENTANTE(5, "representante_tutor", "Representante/Tutor");

    companion object {
        fun fromId(id: Int?): UserRole? {  // ← Cambia Int a Int?
            return values().find { it.id == id }
        }

        fun getAllRoles(): List<UserRole> = values().toList()

    }
}