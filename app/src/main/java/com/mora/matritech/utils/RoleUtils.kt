package com.mora.matritech.utils

object RoleUtils {
    // IDs de roles
    const val ROL_SUPER_ADMIN = 1
    const val ROL_ADMIN = 2
    const val ROL_COORDINADOR = 3
    const val ROL_DOCENTE = 4
    const val ROL_ESTUDIANTE = 5
    const val ROL_REPRESENTANTE = 6

    // Nombres de roles
    const val NOMBRE_SUPER_ADMIN = "Super Admin"
    const val NOMBRE_ADMIN = "Admin"
    const val NOMBRE_COORDINADOR = "Coordinador"
    const val NOMBRE_DOCENTE = "Docente"
    const val NOMBRE_ESTUDIANTE = "Estudiante"
    const val NOMBRE_REPRESENTANTE = "Representante"

    fun isSuperAdmin(rolId: Int?): Boolean {
        return rolId == ROL_SUPER_ADMIN
    }

    fun isAdmin(rolId: Int?): Boolean {
        return rolId == ROL_ADMIN
    }

    fun isCoordinador(rolId: Int?): Boolean {
        return rolId == ROL_COORDINADOR
    }

    fun isDocente(rolId: Int?): Boolean {
        return rolId == ROL_DOCENTE
    }

    fun isEstudiante(rolId: Int?): Boolean {
        return rolId == ROL_ESTUDIANTE
    }

    fun isRepresentante(rolId: Int?): Boolean {
        return rolId == ROL_REPRESENTANTE
    }

    fun hasAdminPrivileges(rolId: Int?): Boolean {
        return rolId != null && rolId in listOf(ROL_SUPER_ADMIN, ROL_ADMIN)
    }

    fun hasStaffPrivileges(rolId: Int?): Boolean {
        return rolId != null && rolId in listOf(ROL_SUPER_ADMIN, ROL_ADMIN, ROL_COORDINADOR, ROL_DOCENTE)
    }

    fun getRoleName(rolId: Int?): String {
        return when (rolId) {
            ROL_SUPER_ADMIN -> NOMBRE_SUPER_ADMIN
            ROL_ADMIN -> NOMBRE_ADMIN
            ROL_COORDINADOR -> NOMBRE_COORDINADOR
            ROL_DOCENTE -> NOMBRE_DOCENTE
            ROL_ESTUDIANTE -> NOMBRE_ESTUDIANTE
            ROL_REPRESENTANTE -> NOMBRE_REPRESENTANTE
            else -> "Desconocido"
        }
    }
}

// Extension functions para facilitar el uso
fun Int?.isSuperAdmin() = RoleUtils.isSuperAdmin(this)
fun Int?.isAdmin() = RoleUtils.isAdmin(this)
fun Int?.hasAdminPrivileges() = RoleUtils.hasAdminPrivileges(this)
fun Int?.hasStaffPrivileges() = RoleUtils.hasStaffPrivileges(this)