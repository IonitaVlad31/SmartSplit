package com.example.smartsplit.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsplit.data.model.User
import com.example.smartsplit.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendsState(
    val isLoading: Boolean = true,
    val friends: List<User> = emptyList(),
    val error: String? = null,
    val isAddingFriend: Boolean = false,
    val addFriendSuccess: Boolean = false
)

class FriendsViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()
    private val _state = MutableStateFlow(FriendsState())
    val state: StateFlow<FriendsState> = _state.asStateFlow()

    init {
        loadFriends()
    }

    fun loadFriends() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val user = repository.getUser(uid)
                if (user != null) {
                    val friendsList = repository.getFriends(user.friendIds)
                    _state.value = _state.value.copy(isLoading = false, friends = friendsList)
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "User not found")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun addFriendByHandle(handle: String) {
        val uid = auth.currentUser?.uid ?: return
        if (handle.isBlank()) {
            _state.value = _state.value.copy(error = "Introdu un handle valid")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isAddingFriend = true, error = null, addFriendSuccess = false)
            try {
                val friend = repository.searchUserByHandle(handle)
                if (friend == null) {
                    _state.value = _state.value.copy(isAddingFriend = false, error = "Utilizatorul nu a fost găsit")
                } else if (friend.id == uid) {
                    _state.value = _state.value.copy(isAddingFriend = false, error = "Nu te poți adăuga pe tine")
                } else {
                    repository.addFriend(uid, friend.id)
                    _state.value = _state.value.copy(isAddingFriend = false, addFriendSuccess = true)
                    loadFriends() 
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isAddingFriend = false, error = e.message)
            }
        }
    }

    fun resetAddFriendState() {
        _state.value = _state.value.copy(error = null, addFriendSuccess = false)
    }

    suspend fun getChatRoomWith(friendId: String): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            repository.getOrCreateDirectChat(uid, friendId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getDirectGroupWith(friendId: String, friendName: String): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            repository.getOrCreateDirectGroup(uid, friendId, friendName)
        } catch (e: Exception) {
            null
        }
    }
}
