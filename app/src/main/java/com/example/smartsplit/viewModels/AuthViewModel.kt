package com.example.smartsplit.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsplit.data.model.User
import com.example.smartsplit.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val repository = FirebaseRepository()

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
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        // Create the User document locally
                        val newUser = User(
                            id = firebaseUser.uid,
                            email = email,
                            name = email.substringBefore("@") // Default name based on email prefix
                        )
                        // Save it to Firestore
                        viewModelScope.launch {
                            try {
                                repository.saveUser(newUser)
                                Log.e("Register", "Success via Firebase & Firestore")
                                _authState.value = AuthState(isAuthenticated = true)
                            } catch (e: Exception) {
                                Log.e("Register", "Failed saving to Firestore: ${e.message}")
                                _authState.value = AuthState(errorMessage = "Cont creat, dar a eșuat salvarea profilului.")
                            }
                        }
                    }
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
