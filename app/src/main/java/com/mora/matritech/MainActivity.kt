package com.mora.matritech

import LoginScreen
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mora.matritech.data.SupabaseClient
import com.mora.matritech.ui.login.LoginScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SupabaseClient.baseUrl = getString(R.string.supabase_url)
        SupabaseClient.anonKey = getString(R.string.supabase_anon_key)

        setContent {
            LoginScreen(
                onLoginWithEmail = { email, pass -> signInEmail(email, pass) },
                onGoogleSignIn = { startGoogleSignIn() }
            )
        }

        handleIntentIfCallback(intent)
    }

    private fun startGoogleSignIn() {
        val redirect = getString(R.string.supabase_redirect)
        val url = SupabaseClient.getGoogleAuthUrl(redirect)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun handleIntentIfCallback(intent: Intent?) {
        val data = intent?.data
        val token = SupabaseClient.extractAccessTokenFromCallback(data)
        if (!token.isNullOrEmpty()) {
            Toast.makeText(this, "Access token obtenido", Toast.LENGTH_SHORT).show()
            // guardar token / navegar
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntentIfCallback(intent)
    }

    private fun signInEmail(email: String, password: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = SupabaseClient.signInWithEmail(email, password)
            result.fold(
                onSuccess = { resp -> Toast.makeText(this@MainActivity, "OK: $resp", Toast.LENGTH_LONG).show() },
                onFailure = { err -> Toast.makeText(this@MainActivity, "Error: ${err.message}", Toast.LENGTH_LONG).show() }
            )
        }
    }
}