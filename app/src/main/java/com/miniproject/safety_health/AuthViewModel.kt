package com.miniproject.safety_health



import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth

    var user by mutableStateOf(auth.currentUser)
        private set

    var authErrorMessage by mutableStateOf<String?>(null)
        private set

    private fun handleException(e: Exception): String = when (e) {
        is FirebaseAuthWeakPasswordException ->
            "Password is too weak. It must be at least 6 characters."
        is FirebaseAuthInvalidCredentialsException ->
            "Invalid email address or badly formatted."
        is FirebaseAuthInvalidUserException ->
            "No account found with this email."
        is FirebaseAuthUserCollisionException ->
            "This email is already registered."
        else ->
            "Authentication failed: ${e.localizedMessage}"
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        authErrorMessage = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser
                    onSuccess()
                } else {
                    val err = task.exception
                    authErrorMessage = err?.let { handleException(it) }
                }
            }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        authErrorMessage = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser
                    onSuccess()
                } else {
                    val err = task.exception
                    authErrorMessage = err?.let { handleException(it) }
                }
            }
    }

    fun logout() {
        auth.signOut()
        user = null
    }
}
