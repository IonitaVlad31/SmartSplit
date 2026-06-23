package com.example.smartsplit.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
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

    fun login(email: String, password: String) {
        _authState.value = AuthState(isLoading = true)

        viewModelScope.launch {
            // Mocking a network delay for Firebase auth
            delay(1500)
            
            if (email.isNotBlank() && password.length > 4) {
                Log.e("Login", "Success (Mock)")
                _authState.value = AuthState(isAuthenticated = true)
            } else {
                Log.e("Login", "Failed (Mock)")
                _authState.value = AuthState(errorMessage = "Invalid credentials")
            }
        }
    }

    fun register(email: String, password: String) {
        _authState.value = AuthState(isLoading = true)

        viewModelScope.launch {
            delay(1500)
            Log.e("Register", "Success (Mock)")
            _authState.value = AuthState(isAuthenticated = true)
        }
    }
    
    fun logout() {
        _authState.value = AuthState()
    }
}
