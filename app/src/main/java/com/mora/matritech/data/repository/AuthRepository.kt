package com.mora.matritech.data.repository

import com.mora.matritech.data.remote.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

// Modelo para la tabla usuarios (actualizado para UUID)
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
     * REGISTRAR un nuevo usuario
     * Supabase Auth + Trigger automáticamente crea el registro en usuarios
     */


    /**
     * INICIAR SESIÓN
     */
    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser != null) {
                val usuario = getCurrentUser()
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
    suspend fun signUp(
        email: String,
        password: String,
        nombre: String
    ): AuthResult {
        return try {
            println("🔵 AuthRepository.signUp iniciado")
            println("📧 Email: $email")
            println("👤 Nombre: $nombre")

            // Registrar en Supabase Auth
            println("🔄 Llamando a supabase.auth.signUpWith...")
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            println("✅ signUpWith completado")

            // El trigger handle_new_user() creará automáticamente el registro en usuarios

            // Obtener el usuario recién creado
            println("🔄 Obteniendo usuario actual...")
            val currentUser = supabase.auth.currentUserOrNull()
            println("👤 Usuario actual: ${currentUser?.id}")

            if (currentUser != null) {
                println("🔄 Obteniendo datos completos del usuario...")
                val usuario = getCurrentUser()
                println("✅ Usuario completo obtenido: ${usuario?.email}")
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

                else -> "Error al registrar: ${e.message}"
            }
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * OBTENER USUARIO ACTUAL con sus datos completos
     */
    suspend fun getCurrentUser(): Usuario? {
        return try {
            val authUser = supabase.auth.currentUserOrNull() ?: return null

            // Obtener datos completos de la tabla usuarios
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

            rol.nombre

        } catch (e: Exception) {
            println("Error al obtener rol: ${e.message}")
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

            AuthResult.Success(userId)
        } catch (e: Exception) {
            AuthResult.Error("Error al actualizar perfil: ${e.message}")
        }
    }
}