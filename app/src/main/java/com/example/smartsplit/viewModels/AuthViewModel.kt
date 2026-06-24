package com.example.smartsplit.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        // Check if user is already logged in when ViewModel is created
        if (auth.currentUser != null) {
            _authState.value = AuthState(isAuthenticated = true)
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState(errorMessage = "Te rugăm să completezi ambele câmpuri.")
            return
        }

        _authState.value = AuthState(isLoading = true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e("Login", "Success via Firebase")
                    _authState.value = AuthState(isAuthenticated = true)
                } else {
                    Log.e("Login", "Failed: ${task.exception?.message}")
                    _authState.value = AuthState(errorMessage = task.exception?.localizedMessage ?: "Eroare la autentificare")
                }
            }
    }

    fun register(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _authState.value = AuthState(errorMessage = "Parola trebuie să aibă minim 6 caractere.")
            return
        }

        _authState.value = AuthState(isLoading = true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e("Register", "Success via Firebase")
                    _authState.value = AuthState(isAuthenticated = true)
                } else {
                    Log.e("Register", "Failed: ${task.exception?.message}")
                    _authState.value = AuthState(errorMessage = task.exception?.localizedMessage ?: "Eroare la înregistrare")
                }
            }
    }
    
    fun logout() {
        auth.signOut()
        _authState.value = AuthState()
    }
}
