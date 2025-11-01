package com.mora.matritech.data

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object SupabaseClient {

    var baseUrl = "https://yakpqvzggrtkltjopvoh.supabase.co"
    var anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inlha3BxdnpnZ3J0a2x0am9wdm9oIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk2MTM5MzQsImV4cCI6MjA3NTE4OTkzNH0.fzt9YWkI127zJ5Kj0K3_l9BC9y6hbzkeRUxfBEVJSZw"


    private val client = OkHttpClient()
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    suspend fun signInWithEmail(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/auth/v1/token?grant_type=password"
            val bodyJson = JSONObject()
                .put("email", email)
                .put("password", password)
                .toString()
                .toRequestBody(jsonMedia)

            val req = Request.Builder()
                .url(url)
                .post(bodyJson)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .build()

            client.newCall(req).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (resp.isSuccessful) Result.success(text) else Result.failure(Exception("Error ${resp.code}: $text"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getGoogleAuthUrl(redirectUri: String): String {
        return Uri.parse("$baseUrl/auth/v1/authorize")
            .buildUpon()
            .appendQueryParameter("provider", "google")
            .appendQueryParameter("redirect_to", redirectUri)
            .build()
            .toString()
    }

    fun extractAccessTokenFromCallback(uri: Uri?): String? {
        val frag = uri?.fragment ?: return null
        return frag.split("&").mapNotNull {
            val parts = it.split("="); if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()["access_token"]
    }
}