package com.mora.matritech.screens.admin

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview

// screens/AdminScreen.kt
@Composable
fun AdminScreen() { Text("Pantalla del Administrador") }



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminScreenText(){
    AdminScreen()
}