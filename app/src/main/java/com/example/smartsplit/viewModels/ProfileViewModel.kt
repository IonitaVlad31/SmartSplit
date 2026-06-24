package com.example.smartsplit.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsplit.data.model.User
import com.example.smartsplit.data.repository.FirebaseRepository
import com.example.smartsplit.data.repository.UserPreferencesRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val saveError: String? = null,
    val isSaving: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val repository = FirebaseRepository()
    private val userPrefsRepo = UserPreferencesRepository(application)

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    val notificationsEnabled: StateFlow<Boolean> = userPrefsRepo.notificationsEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    init {
        fetchProfile()
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPrefsRepo.setNotificationsEnabled(enabled)
        }
    }

    fun fetchProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _state.value = _state.value.copy(isLoading = false, saveError = "Nu ești autentificat")
            return
        }

        viewModelScope.launch {
            try {
                val user = repository.getUser(uid)
                _state.value = _state.value.copy(isLoading = false, user = user)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, saveError = e.message)
            }
        }
    }

    fun updateProfile(newName: String, newHandle: String, onSuccess: () -> Unit) {
        val currentUser = _state.value.user ?: return

        var cleanHandle = newHandle.trim()
        if (cleanHandle.startsWith("@")) cleanHandle = cleanHandle.substring(1)
        
        if (newName.isBlank()) {
            _state.value = _state.value.copy(saveError = "Numele nu poate fi gol.")
            return
        }

        _state.value = _state.value.copy(isSaving = true, saveError = null)

        viewModelScope.launch {
            try {
                if (cleanHandle.isNotBlank() && cleanHandle != currentUser.handle) {
                    val taken = repository.isHandleTaken(cleanHandle, currentUser.id)
                    if (taken) {
                        _state.value = _state.value.copy(
                            isSaving = false,
                            saveError = "Acest handle (@$cleanHandle) este deja folosit de altcineva."
                        )
                        return@launch
                    }
                }

                val updates = mapOf(
                    "name" to newName.trim(),
                    "handle" to cleanHandle
                )

                repository.updateUser(currentUser.id, updates)

                _state.value = _state.value.copy(
                    isSaving = false,
                    user = currentUser.copy(name = newName.trim(), handle = cleanHandle)
                )
                onSuccess()
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, saveError = "Eroare la salvare: ${e.message}")
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(saveError = null)
    }
}
