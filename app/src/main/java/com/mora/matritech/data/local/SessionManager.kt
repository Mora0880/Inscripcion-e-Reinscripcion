package com.mora.matritech.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveSession(userId: String?, role: String) {
        prefs.edit().apply {
            putBoolean("isLoggedIn", true)
            putString("userId", userId)
            putString("userRole", role)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }

    fun getUserRole(): String? {
        return prefs.getString("userRole", null)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
