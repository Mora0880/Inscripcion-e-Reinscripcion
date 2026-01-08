package com.mora.matritech.data.repository

import com.mora.matritech.data.remote.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// Modelo para la tabla usuarios
@Serializable
data class Usuario(
    val id: String? = null,
    val email: String,
    val nombre: String,
    val rol_id: Int? = null,
    val institucion_id: String? = null,
    val es_super_admin: Boolean = false,
    val activo: Boolean = true,
    val fecha_creacion: String? = null
)

@Serializable
data class Rol(
    val id: Int,
    val nombre: String
)

sealed class AuthResult {
    data class Success(val userId: String, val user: Usuario? = null) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

class AuthRepository {

    /**
     * REGISTRAR un nuevo usuario CON ROL
     * @param email Correo electrónico
     * @param password Contraseña
     * @param nombre Nombre completo
     * @param roleId ID del rol seleccionado (1-5)
     */
    suspend fun signUp(
        email: String,
        password: String,
        nombre: String,
        roleId: Int
    ): AuthResult {
        return try {
            println("🔵 AuthRepository.signUp iniciado")
            println("📧 Email: $email")
            println("👤 Nombre: $nombre")
            println("🎭 Role ID: $roleId")

            // 1. Registrar en Supabase Auth
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password

                // ASÍ SE HACE AHORA (2025)
                data = buildJsonObject {
                    put("nombre", nombre)
                    put("rol_id", roleId)
                }
            }

            println("✅ signUpWith completado")

            // 2. El trigger crea el registro base en usuarios

            // 3. Obtener el usuario recién creado
            val currentUser = supabase.auth.currentUserOrNull()
            println("👤 Usuario actual: ${currentUser?.id}")

            if (currentUser != null) {
                // 4. Actualizar con el nombre y rol seleccionado
                println("🔄 Actualizando usuario con nombre y rol...")
                supabase.from("usuarios")
                    .update({
                        set("nombre", nombre)
                        set("rol_id", roleId)
                    }) {
                        filter {
                            eq("id", currentUser.id)
                        }
                    }

                println("✅ Usuario actualizado con rol $roleId")

                // 5. Obtener datos completos
                val usuario = getCurrentUser()
                println("✅ Usuario completo: ${usuario?.email}, Rol: ${usuario?.rol_id}")

                AuthResult.Success(currentUser.id, usuario)
            } else {
                println("❌ No se pudo obtener currentUser")
                AuthResult.Error("Error al obtener usuario después del registro")
            }

        } catch (e: Exception) {
            println("❌ EXCEPCIÓN en signUp: ${e.message}")
            e.printStackTrace()

            val errorMessage = when {
                e.message?.contains("already registered") == true ->
                    "Este correo ya está registrado"
                e.message?.contains("email") == true && e.message?.contains("invalid") == true ->
                    "Correo electrónico inválido"
                e.message?.contains("password") == true ->
                    "La contraseña debe tener al menos 6 caracteres"
                e.message?.contains("duplicate key") == true ->
                    "Este correo ya está registrado"
                else -> "Error al registrar: ${e.message}"
            }
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * INICIAR SESIÓN y obtener datos con rol
     */
    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            println("🔵 Iniciando sesión...")

            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser != null) {
                val usuario = getCurrentUser()
                println("✅ Login exitoso - Rol ID: ${usuario?.rol_id}")
                AuthResult.Success(currentUser.id, usuario)
            } else {
                AuthResult.Error("Error al obtener datos del usuario")
            }

        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Invalid login credentials") == true ->
                    "Correo o contraseña incorrectos"
                e.message?.contains("Email not confirmed") == true ->
                    "Debes confirmar tu correo electrónico"
                e.message?.contains("Invalid") == true ->
                    "Credenciales inválidas"
                else -> "Error al iniciar sesión: ${e.message}"
            }
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * CERRAR SESIÓN
     */
    suspend fun signOut(): AuthResult {
        return try {
            supabase.auth.signOut()
            println("✅ Sesión cerrada")
            AuthResult.Success("", null)
        } catch (e: Exception) {
            println("❌ Error al cerrar sesión: ${e.message}")
            AuthResult.Error("Error al cerrar sesión: ${e.message}")
        }
    }

    /**
     * OBTENER USUARIO ACTUAL con sus datos completos
     */
    suspend fun getCurrentUser(): Usuario? {
        return try {
            val authUser = supabase.auth.currentUserOrNull() ?: return null

            supabase.from("usuarios")
                .select {
                    filter {
                        eq("id", authUser.id)
                    }
                }
                .decodeSingle<Usuario>()

        } catch (e: Exception) {
            println("Error al obtener usuario: ${e.message}")
            null
        }
    }

    /**
     * OBTENER ROL del usuario actual
     */
    suspend fun getCurrentUserRole(): String? {
        return try {
            val usuario = getCurrentUser() ?: return null
            val rolId = usuario.rol_id ?: return null

            val rol = supabase.from("roles")
                .select {
                    filter {
                        eq("id", rolId)
                    }
                }
                .decodeSingle<Rol>()

            println("🎭 Rol obtenido: ${rol.nombre}")
            rol.nombre

        } catch (e: Exception) {
            println("Error al obtener rol: ${e.message}")
            null
        }
    }

    /**
     * OBTENER ID del rol del usuario actual
     */
    suspend fun getCurrentUserRoleId(): Int? {
        return try {
            val usuario = getCurrentUser()
            usuario?.rol_id
        } catch (e: Exception) {
            println("Error al obtener rol ID: ${e.message}")
            null
        }
    }

    /**
     * VERIFICAR SI ESTÁ AUTENTICADO
     */
    fun isAuthenticated(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }

    /**
     * OBTENER ID DEL USUARIO ACTUAL
     */
    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    /**
     * RESETEAR CONTRASEÑA
     */
    suspend fun resetPassword(email: String): AuthResult {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            AuthResult.Success("", null)
        } catch (e: Exception) {
            AuthResult.Error("Error al enviar correo: ${e.message}")
        }
    }

    /**
     * ACTUALIZAR PERFIL
     */
    suspend fun updateProfile(nombre: String): AuthResult {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("Usuario no autenticado")

            supabase.from("usuarios")
                .update({
                    set("nombre", nombre)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }

            AuthResult.Success(userId, null)
        } catch (e: Exception) {
            AuthResult.Error("Error al actualizar perfil: ${e.message}")
        }
    }
}