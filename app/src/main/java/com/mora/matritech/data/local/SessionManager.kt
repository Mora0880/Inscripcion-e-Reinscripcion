package com.mora.matritech.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    /**
     * Guarda la sesión del usuario
     * @param userId ID del usuario (UUID de Supabase Auth)
     * @param role Rol del usuario (student, admin, etc.)
     */
    fun saveSession(userId: String?, role: String) {
        prefs.edit().apply {
            putBoolean("isLoggedIn", true)
            putString("userId", userId)
            putString("userRole", role)
            apply()
        }
    }

    /**
     * Verifica si hay una sesión activa
     * @return true si el usuario está logueado
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }

    /**
     * Obtiene el rol del usuario actual
     * @return String con el rol o null si no hay sesión
     */
    fun getUserRole(): String? {
        return prefs.getString("userRole", null)
    }

    /**
     * Obtiene el ID del usuario actual
     * @return String con el userId o null si no hay sesión
     */
    fun getUserId(): String? {
        return prefs.getString("userId", null)
    }

    /**
     * Cierra la sesión y limpia todos los datos guardados
     */
    fun logout() {
        prefs.edit().clear().apply()
    }
}