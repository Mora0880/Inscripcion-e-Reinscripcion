package com.mora.matritech.ui.Splash

import android.graphics.Color
import android.graphics.Color.blue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),// Un morado oscuro
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "MatriTech",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,

        )
    }
}

    // Aquí tu diseño actual del splash (texto, color, logo, etc.)



@Composable
fun SplashScreenContent() {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Mi App",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Preview(showBackground = true,)
@Composable
fun SplashPreview() {
    MaterialTheme {
        SplashScreenContent()
    }
}

