package com.miniproject.safety_health

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(20.dp))

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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.height(12.dp))

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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        val trimmedEmail = email.trim()
                        val trimmedPassword = password.trim()

                        // Validate input
                        when {
                            trimmedEmail.isEmpty() -> {
                                localError = "Email can't be empty"
                            }
                            !trimmedEmail.contains("@") -> {
                                localError = "Please enter a valid email"
                            }
                            trimmedPassword.length < 6 -> {
                                localError = "Password must be at least 6 characters"
                            }
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
                        .height(50.dp)
                ) {
                    Text(
                        if (isLogin) "Login" else "Register",
                        color = Color.White
                    )
                }

                TextButton(
                    onClick = { isLogin = !isLogin },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        if (isLogin)
                            "Don't have an account? Register"
                        else
                            "Already have an account? Login",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Show any error
                localError?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(text = it, color = Color.Red)
                }

                firebaseError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(text = it, color = Color.Red)
                }
            }
        }
    }
}