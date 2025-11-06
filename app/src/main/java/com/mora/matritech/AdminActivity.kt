package com.mora.matritech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mora.matritech.ui.theme.MatriTechTheme
import com.mora.matritech.ui.theme.admin.AdminScreen

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MatriTechTheme {
                AdminScreen()
            }
        }
    }
}
