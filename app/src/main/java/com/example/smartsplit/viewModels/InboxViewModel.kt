package com.example.smartsplit.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsplit.data.model.ChatRoom
import com.example.smartsplit.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InboxState(
    val isLoading: Boolean = true,
    val chatRooms: List<ChatRoom> = emptyList(),
    val otherUsers: Map<String, com.example.smartsplit.data.model.User> = emptyMap(),
    val error: String? = null,
    val currentUserId: String = ""
)

class InboxViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()
    private val _state = MutableStateFlow(InboxState())
    val state: StateFlow<InboxState> = _state.asStateFlow()

    init {
        observeChats()
    }

    private fun observeChats() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _state.value = InboxState(isLoading = false, error = "Nu ești autentificat")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(currentUserId = currentUserId)
            try {
                repository.observeUserChatRooms(currentUserId).collect { rooms ->
                    val otherIds = rooms.flatMap { it.participantIds }.filter { it != currentUserId }.distinct()
                    val currentMap = _state.value.otherUsers.toMutableMap()
                    val missingIds = otherIds.filter { !currentMap.containsKey(it) }
                    
                    if (missingIds.isNotEmpty()) {
                        val fetchedUsers = repository.getFriends(missingIds)
                        fetchedUsers.forEach { currentMap[it.id] = it }
                    }
                    
                    _state.value = _state.value.copy(
                        isLoading = false, 
                        chatRooms = rooms,
                        otherUsers = currentMap
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
