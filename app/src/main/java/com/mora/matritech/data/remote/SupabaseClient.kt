package com.mora.matritech.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.functions.Functions
import com.mora.matritech.BuildConfig

object SupabaseClient {

    // Lazy initialization - el cliente solo se crea cuando se usa por primera vez
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Functions)
        }
    }
}

// También con lazy para evitar inicialización prematura
val supabase by lazy { SupabaseClient.client }