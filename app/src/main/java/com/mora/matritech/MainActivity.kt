package com.mora.matritech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.mora.matritech.ui.Splash.SplashScreen
import com.mora.matritech.ui.home.HomeScreen // (lo crearás luego)
import com.mora.matritech.ui.theme.MatriTechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MatriTechTheme {
                var showHome = remember { mutableStateOf(false) }

                if (showHome.value) {
                    HomeScreen()
                } else {
                    SplashScreen(onSplashFinished = {
                        showHome.value = true
                    })
                }
            }
        }
    }
}
