package com.example.smartsplit.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsplit.data.model.Group
import com.example.smartsplit.data.model.User
import com.example.smartsplit.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateGroupState(
    val isLoading: Boolean = false,
    val friends: List<User> = emptyList(),
    val selectedFriendIds: Set<String> = emptySet(),
    val groupName: String = "",
    val isCreating: Boolean = false,
    val successGroupId: String? = null,
    val error: String? = null
)

class CreateGroupViewModel : ViewModel() {
    private val repository = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _state = MutableStateFlow(CreateGroupState())
    val state: StateFlow<CreateGroupState> = _state.asStateFlow()

    init {
        loadFriends()
    }

    private fun loadFriends() {
        val uid = auth.currentUser?.uid ?: return
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val currentUser = repository.getUser(uid)
                if (currentUser != null && currentUser.friendIds.isNotEmpty()) {
                    val friendsList = repository.getFriends(currentUser.friendIds)
                    _state.value = _state.value.copy(isLoading = false, friends = friendsList)
                } else {
                    _state.value = _state.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun updateGroupName(name: String) {
        _state.value = _state.value.copy(groupName = name)
    }

    fun toggleFriendSelection(friendId: String) {
        val currentSelected = _state.value.selectedFriendIds.toMutableSet()
        if (currentSelected.contains(friendId)) {
            currentSelected.remove(friendId)
        } else {
            currentSelected.add(friendId)
        }
        _state.value = _state.value.copy(selectedFriendIds = currentSelected)
    }

    fun createGroup() {
        val uid = auth.currentUser?.uid ?: return
        val currentName = _state.value.groupName.trim()
        val currentSelected = _state.value.selectedFriendIds
        
        if (currentName.isBlank()) {
            _state.value = _state.value.copy(error = "Numele grupului nu poate fi gol.")
            return
        }

        if (currentSelected.isEmpty()) {
            _state.value = _state.value.copy(error = "Selectează cel puțin un prieten.")
            return
        }

        _state.value = _state.value.copy(isCreating = true, error = null)

        viewModelScope.launch {
            try {
                val memberIds = currentSelected.toMutableList()
                memberIds.add(uid) // Add self
                
                val newGroup = Group(
                    name = currentName,
                    memberIds = memberIds
                )
                
                val groupId = repository.createGroup(newGroup)
                _state.value = _state.value.copy(isCreating = false, successGroupId = groupId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isCreating = false, error = e.message)
            }
        }
    }
}
