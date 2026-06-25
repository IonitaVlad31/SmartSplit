package com.example.smartsplit.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsplit.data.model.Message
import com.example.smartsplit.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore

data class ChatState(
    val isLoading: Boolean = true,
    val messages: List<Message> = emptyList(),
    val error: String? = null,
    val currentUserId: String = "",
    val chatTitle: String = "Chat"
)

class ChatViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun loadChat(chatId: String) {
        val uid = auth.currentUser?.uid ?: ""
        _state.value = _state.value.copy(currentUserId = uid)
        
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("chatRooms").document(chatId).get().await()
                val room = snapshot.toObject(com.example.smartsplit.data.model.ChatRoom::class.java)
                if (room != null) {
                    if (room.name.isNotBlank()) {
                        _state.value = _state.value.copy(chatTitle = room.name)
                    } else {
                        val otherId = room.participantIds.find { it != uid }
                        if (otherId != null) {
                            val otherUser = repository.getUser(otherId)
                            if (otherUser != null) {
                                _state.value = _state.value.copy(chatTitle = "${otherUser.name} (@${otherUser.handle})")
                            }
                        }
                    }
                }

                repository.observeMessages(chatId).collect { msgs ->
                    _state.value = _state.value.copy(isLoading = false, messages = msgs)
                }
            } catch (e: Exception) {
                 _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun sendMessage(chatId: String, text: String) {
        val uid = auth.currentUser?.uid ?: return
        if (text.isBlank()) return
        
        val msg = Message(
            chatId = chatId,
            senderId = uid,
            text = text.trim()
        )
        
        viewModelScope.launch {
            try {
                repository.sendMessage(msg)
            } catch (e: Exception) {
                
            }
        }
    }
}
