// kotlin
package com.mora.matritech

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mora.matritech.data.SupabaseClient
import com.mora.matritech.ui.login.LoginScreen
import com.mora.matritech.ui.theme.MatriTechTheme
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import com.mora.matritech.ui.Splash.SplashScreen
import com.mora.matritech.ui.theme.Register.RegisterScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SupabaseClient.baseUrl = getString(R.string.supabase_url)
        SupabaseClient.anonKey = getString(R.string.supabase_anon_key)

        setContent {
            MatriTechTheme {
                // Uso de argumentos posicionales para evitar errores de nombres de parámetro
                LoginScreen(
                    { email, pass -> signInEmail(email, pass) },
                    { startGoogleSignIn() }
                )
            }
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

        composable(NavRoutes.register.route) {
            RegisterScreen(
                onRegisterClick = { email, password, name ->
                    // Aquí luego llamarás Supabase para crear el usuario
                },
                onLoginClick = {
                    navController.navigate("login") {
                        popUpTo(NavRoutes.register.route) { inclusive = true }
                    }
                }
            )
        }


        // Login Screen
        composable(NavRoutes.Login.route) {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                }
            )
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
