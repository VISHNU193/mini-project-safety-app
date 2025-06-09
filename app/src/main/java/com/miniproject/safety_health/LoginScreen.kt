package com.miniproject.safety_health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniproject.safety_health.ui.theme.HealthMonitorTheme

@Composable
fun LoginScreen(viewModel: AuthViewModel, onLoginSuccess: () -> Unit) {
    HealthMonitorTheme {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLogin by remember { mutableStateOf(true) }
        var localError by remember { mutableStateOf<String?>(null) }
        var isProcessing by remember { mutableStateOf(false) }

        val firebaseError = viewModel.authErrorMessage

        // Custom colors for dark background
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.LightGray,
            focusedBorderColor = Color.Cyan,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color.Cyan,
            unfocusedLabelColor = Color.Gray,
            cursorColor = Color.Cyan,
            errorBorderColor = Color.Red,
            errorLabelColor = Color.Red
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isLogin) "Login" else "Register",
                    fontSize = 26.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        localError = null
                    },
                    label = { Text("Email") },
                    singleLine = true,
                    isError = localError?.contains("email", true) == true ||
                            firebaseError?.contains("email", true) == true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        localError = null
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = localError?.contains("password", true) == true ||
                            firebaseError?.contains("password", true) == true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val trimmedEmail = email.trim()
                        val trimmedPassword = password.trim()

                        when {
                            trimmedEmail.isEmpty() -> localError = "Email can't be empty"
                            !trimmedEmail.contains("@") -> localError = "Please enter a valid email"
                            trimmedPassword.length < 6 -> localError = "Password must be at least 6 characters"
                            else -> {
                                localError = null
                                isProcessing = true
                                if (isLogin) {
                                    viewModel.login(trimmedEmail, trimmedPassword) {
                                        isProcessing = false
                                        onLoginSuccess()
                                    }
                                } else {
                                    viewModel.register(trimmedEmail, trimmedPassword) {
                                        isProcessing = false
                                        onLoginSuccess()
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Cyan,
                        contentColor = Color.Black
                    )
                ) {
                    Text(if (isLogin) "Login" else "Register")
                }

                TextButton(
                    onClick = { isLogin = !isLogin },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        text = if (isLogin)
                            "Don't have an account? Register"
                        else
                            "Already have an account? Login",
                        color = Color.LightGray
                    )
                }

                // Error messages
                if (!localError.isNullOrEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = localError!!,
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                firebaseError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}