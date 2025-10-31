package com.mora.matritech.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mora.matritech.ui.theme.MatriTechTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import com.mora.matritech.R
import androidx.compose.ui.draw.clip
@Composable
fun LogoImage(){
    Image(
        painter = painterResource(id = R.drawable.ic_launcher_background),
        contentDescription = "MatriTech",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)

    )
}
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {}
) {
    var useremail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center

    ) {
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            LogoImage()

            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = useremail,
                onValueChange = { useremail = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (useremail.isNotBlank() && password.isNotBlank()) {
                        onLoginSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C3FF),
                    contentColor = Color.Black
                ),
                shape = (RoundedCornerShape(5.dp)),
                modifier = Modifier
                    .padding(10.dp)
                    .height(50.dp)
                    .fillMaxWidth()
            ) {
                Text("Ingresar")
            }
            Button(
                onClick = {
                    if (useremail.isBlank() && password.isBlank()){
                        onLoginSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FF00),
                    contentColor = Color.Black
                ),
                shape = (RoundedCornerShape(5.dp)),
                modifier = Modifier
                    .padding(10.dp)
                    .height(50.dp)
                    .fillMaxWidth()


            ) {
                Text(
                    ("Google")
                )
            }

        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenView(){
    MatriTechTheme {
        LoginScreen()
    }
}