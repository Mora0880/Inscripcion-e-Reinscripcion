package com.mora.matritech.data.repository

import com.mora.matritech.data.remote.supabase
import com.mora.matritech.model.UserData
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Repository para gestión de usuarios (CRUD)
 * Solo para uso del Admin y SuperAdmin
 * ACTUALIZADO: Incluye funcionalidad para asignar instituciones
 */
class UserRepository {

    /**
     * OBTENER TODOS LOS USUARIOS con filtros opcionales
     */
    suspend fun getAllUsers(
        roleFilter: Int? = null,
        activeOnly: Boolean? = null,
        institucionFilter: String? = null
    ): Result<List<UserData>> {
        return try {
            println("🔍 Obteniendo usuarios... roleFilter=$roleFilter, activeOnly=$activeOnly, institucionFilter=$institucionFilter")

            val users = supabase.from("usuarios")
                .select {
                    filter {
                        roleFilter?.let { eq("rol_id", it) }
                        activeOnly?.let { eq("activo", it) }
                        institucionFilter?.let { eq("institucion_id", it) }
                    }
                }
                .decodeList<UserData>()

            println("✅ ${users.size} usuarios obtenidos")
            Result.success(users)

        } catch (e: Exception) {
            println("❌ Error al obtener usuarios: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * BUSCAR USUARIOS por nombre o email
     */
    suspend fun searchUsers(query: String): Result<List<UserData>> {
        return try {
            if (query.isBlank()) {
                return getAllUsers()
            }

            val users = supabase.from("usuarios")
                .select {
                    filter {
                        or {
                            ilike("nombre", "%$query%")
                            ilike("email", "%$query%")
                        }
                    }
                }
                .decodeList<UserData>()

            println("🔍 Búsqueda '$query': ${users.size} resultados")
            Result.success(users)

        } catch (e: Exception) {
            println("❌ Error en búsqueda: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * CREAR NUEVO USUARIO con rol usando Edge Function
     * Esto evita cerrar la sesión del admin
     */
    suspend fun createUser(
        email: String,
        password: String,
        nombre: String,
        roleId: Int,
        institucionId: String? = null
    ): Result<UserData> {
        return try {
            println("➕ Creando usuario via Edge Function: $email, rol: $roleId")

            // Llamar a la Edge Function
            val response = supabase.functions.invoke(
                function = "create-user",
                body = buildJsonObject {
                    put("email", email)
                    put("password", password)
                    put("nombre", nombre)
                    put("rol_id", roleId)
                    institucionId?.let { put("institucion_id", it) }
                }
            )

            // Verificar respuesta
            println("📡 Response status: ${response.status}")

            if (response.status.value !in 200..299) {
                println("❌ Error al crear usuario - Status: ${response.status}")
                throw Exception("Error al crear usuario: Status ${response.status.value}")
            }

            println("✅ Usuario creado en Edge Function")

            // Esperar un momento para que se complete la creación
            delay(1000)

            // Buscar el usuario por email
            val users = supabase.from("usuarios")
                .select {
                    filter {
                        eq("email", email)
                    }
                }
                .decodeList<UserData>()

            val user = users.firstOrNull()
                ?: throw Exception("No se pudo obtener el usuario creado")

            println("✅ Usuario creado exitosamente: ${user.email}")
            Result.success(user)

        } catch (e: Exception) {
            println("❌ Error al crear usuario: ${e.message}")
            e.printStackTrace()

            // Si la Edge Function no está disponible, usar método alternativo
            if (e.message?.contains("404") == true ||
                e.message?.contains("not found", ignoreCase = true) == true ||
                e.message?.contains("Function") == true) {
                println("⚠️ Edge Function no disponible, usando método alternativo")
                return createUserFallback(email, password, nombre, roleId, institucionId)
            }

            val message = when {
                e.message?.contains("already exists") == true ||
                        e.message?.contains("already registered") == true ->
                    "Este correo ya está registrado"
                e.message?.contains("password") == true ->
                    "La contraseña debe tener al menos 6 caracteres"
                e.message?.contains("permisos") == true ->
                    "No tienes permisos de administrador"
                else -> e.message ?: "Error desconocido"
            }
            Result.failure(Exception(message))
        }
    }

    /**
     * MÉTODO ALTERNATIVO si la Edge Function no está disponible
     * ADVERTENCIA: Esto cerrará la sesión del admin
     */
    private suspend fun createUserFallback(
        email: String,
        password: String,
        nombre: String,
        roleId: Int,
        institucionId: String? = null
    ): Result<UserData> {
        return try {
            println("⚠️ Usando método alternativo (cerrará sesión del admin)")

            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password

                data = buildJsonObject {
                    put("nombre", nombre)
                    put("rol_id", roleId)
                    institucionId?.let { put("institucion_id", it) }
                }
            }

            val authUser = supabase.auth.currentUserOrNull()
            if (authUser == null) {
                throw Exception("No se pudo crear el usuario")
            }

            // Actualizar información adicional
            supabase.from("usuarios")
                .update({
                    set("nombre", nombre)
                    set("rol_id", roleId)
                    set("activo", true)
                    institucionId?.let { set("institucion_id", it) }
                }) {
                    filter {
                        eq("id", authUser.id)
                    }
                }

            val user = getUserById(authUser.id)
                ?: throw Exception("No se pudo obtener el usuario creado")

            println("✅ Usuario creado con método alternativo")
            Result.success(user)

        } catch (e: Exception) {
            println("❌ Error en método alternativo: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ACTUALIZAR USUARIO (nombre, email, rol, institución)
     * NOTA: No actualiza el email en Auth (requiere admin privileges)
     */
    suspend fun updateUser(
        userId: String,
        nombre: String,
        email: String,
        roleId: Int,
        institucionId: String? = null
    ): Result<UserData> {
        return try {
            println("✏️ Actualizando usuario: $userId")

            // Actualizar en la tabla usuarios
            supabase.from("usuarios")
                .update({
                    set("nombre", nombre)
                    set("email", email)
                    set("rol_id", roleId)
                    if (institucionId != null) {
                        set("institucion_id", institucionId)
                    }
                }) {
                    filter {
                        eq("id", userId)
                    }
                }

            // Obtener el usuario actualizado
            val updatedUser = getUserById(userId)
                ?: throw Exception("No se pudo obtener el usuario actualizado")

            println("✅ Usuario actualizado: ${updatedUser.email}")
            Result.success(updatedUser)

        } catch (e: Exception) {
            println("❌ Error al actualizar usuario: ${e.message}")
            val message = when {
                e.message?.contains("duplicate") == true ->
                    "Este correo ya está en uso"
                else -> e.message ?: "Error al actualizar"
            }
            Result.failure(Exception(message))
        }
    }

    /**
     * ASIGNAR O CAMBIAR INSTITUCIÓN de un usuario
     */
    suspend fun assignInstitucion(
        userId: String,
        institucionId: String?
    ): Result<UserData> {
        return try {
            println("🏫 Asignando institución $institucionId al usuario $userId")

            supabase.from("usuarios")
                .update({
                    if (institucionId != null) {
                        set("institucion_id", institucionId)
                    } else {
                        set("institucion_id", null as String?)
                    }
                }) {
                    filter {
                        eq("id", userId)
                    }
                }

            val updatedUser = getUserById(userId)
                ?: throw Exception("No se pudo obtener el usuario actualizado")

            println("✅ Institución asignada correctamente")
            Result.success(updatedUser)

        } catch (e: Exception) {
            println("❌ Error al asignar institución: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * OBTENER USUARIOS POR INSTITUCIÓN
     */
    suspend fun getUsersByInstitucion(institucionId: String): Result<List<UserData>> {
        return try {
            println("🔍 Obteniendo usuarios de institución: $institucionId")

            val users = supabase.from("usuarios")
                .select {
                    filter {
                        eq("institucion_id", institucionId)
                    }
                }
                .decodeList<UserData>()

            println("✅ ${users.size} usuarios encontrados")
            Result.success(users)

        } catch (e: Exception) {
            println("❌ Error al obtener usuarios por institución: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * DESACTIVAR USUARIO (soft delete)
     */
    suspend fun deactivateUser(userId: String): Result<Boolean> {
        return try {
            println("🚫 Desactivando usuario: $userId")

            supabase.from("usuarios")
                .update({
                    set("activo", false)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }

            println("✅ Usuario desactivado")
            Result.success(true)

        } catch (e: Exception) {
            println("❌ Error al desactivar usuario: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ACTIVAR USUARIO
     */
    suspend fun activateUser(userId: String): Result<Boolean> {
        return try {
            println("✅ Activando usuario: $userId")

            supabase.from("usuarios")
                .update({
                    set("activo", true)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }

            println("✅ Usuario activado")
            Result.success(true)

        } catch (e: Exception) {
            println("❌ Error al activar usuario: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ELIMINAR USUARIO PERMANENTEMENTE (hard delete)
     * ⚠️ USAR CON PRECAUCIÓN
     */
    suspend fun deleteUserPermanently(userId: String): Result<Boolean> {
        return try {
            println("🗑️ Eliminando usuario permanentemente: $userId")

            supabase.from("usuarios")
                .delete {
                    filter {
                        eq("id", userId)
                    }
                }

            println("✅ Usuario eliminado permanentemente")
            Result.success(true)

        } catch (e: Exception) {
            println("❌ Error al eliminar usuario: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * OBTENER USUARIO POR ID
     */
    suspend fun getUserById(userId: String): UserData? {
        return try {
            supabase.from("usuarios")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<UserData>()
        } catch (e: Exception) {
            println("❌ Error al obtener usuario: ${e.message}")
            null
        }
    }

    /**
     * OBTENER ESTADÍSTICAS DE USUARIOS
     */
    suspend fun getUserStats(): Result<UserStats> {
        return try {
            val allUsers = getAllUsers().getOrThrow()

            val stats = UserStats(
                totalUsers = allUsers.size,
                activeUsers = allUsers.count { it.activo },
                inactiveUsers = allUsers.count { !it.activo },
                studentCount = allUsers.count { it.roleId == 3 },
                teacherCount = allUsers.count { it.roleId == 4 },
                adminCount = allUsers.count { it.roleId == 1 },
                coordinatorCount = allUsers.count { it.roleId == 2 },
                representativeCount = allUsers.count { it.roleId == 5 }
            )

            Result.success(stats)
        } catch (e: Exception) {
            println("❌ Error al obtener estadísticas: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * OBTENER ESTADÍSTICAS POR INSTITUCIÓN
     */
    suspend fun getStatsByInstitucion(institucionId: String): Result<UserStats> {
        return try {
            val users = getUsersByInstitucion(institucionId).getOrThrow()

            val stats = UserStats(
                totalUsers = users.size,
                activeUsers = users.count { it.activo },
                inactiveUsers = users.count { !it.activo },
                studentCount = users.count { it.roleId == 3 },
                teacherCount = users.count { it.roleId == 4 },
                adminCount = users.count { it.roleId == 1 },
                coordinatorCount = users.count { it.roleId == 2 },
                representativeCount = users.count { it.roleId == 5 }
            )

            Result.success(stats)
        } catch (e: Exception) {
            println("❌ Error al obtener estadísticas por institución: ${e.message}")
            Result.failure(e)
        }
    }
}

/**
 * Data class para estadísticas
 */
data class UserStats(
    val totalUsers: Int,
    val activeUsers: Int,
    val inactiveUsers: Int,
    val studentCount: Int,
    val teacherCount: Int,
    val adminCount: Int,
    val coordinatorCount: Int,
    val representativeCount: Int
)