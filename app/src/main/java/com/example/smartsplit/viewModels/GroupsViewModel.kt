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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class GroupsViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends.asStateFlow()

    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance.asStateFlow()

    private val _totalOwe = MutableStateFlow(0.0)
    val totalOwe: StateFlow<Double> = _totalOwe.asStateFlow()

    private val _totalOwedToMe = MutableStateFlow(0.0)
    val totalOwedToMe: StateFlow<Double> = _totalOwedToMe.asStateFlow()

    private val _groupBalances = MutableStateFlow<Map<String, Double>>(emptyMap())
    val groupBalances: StateFlow<Map<String, Double>> = _groupBalances.asStateFlow()

    init {
        observeGroups()
    }

    private fun observeGroups() {
        val auth = FirebaseAuth.getInstance()
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                _isLoading.value = true
                viewModelScope.launch {
                    repository.observeUserGroups(user.uid)
                        .catch { e ->
                            _error.value = e.message
                            _isLoading.value = false
                        }
                        .collect { groupList ->
                            _groups.value = groupList
                            _isLoading.value = false
                            calculateBalances(groupList, user.uid)
                        }
                }
                viewModelScope.launch {
                    repository.observeUser(user.uid)
                        .catch {  }
                        .collect { dbUser ->
                            if (dbUser != null) {
                                try {
                                    val userFriends = repository.getFriends(dbUser.friendIds)
                                    _friends.value = userFriends
                                } catch (e: Exception) {
                                    
                                }
                            }
                        }
                }
            } else {
                _groups.value = emptyList()
                _friends.value = emptyList()
            }
        }
        auth.addAuthStateListener(authListener)
    }

    fun createGroup(name: String, memberIds: List<String>) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val allMembers = memberIds.toMutableSet().apply { add(currentUser.uid) }.toList()
                
                val newGroup = Group(
                    name = name,
                    memberIds = allMembers,
                    balances = allMembers.associateWith { 0.0 }
                )
                
                repository.createGroup(newGroup)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun refreshBalances() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val currentGroups = _groups.value
        calculateBalances(currentGroups, user.uid)
    }

    private fun calculateBalances(groups: List<Group>, currentUserId: String) {
        viewModelScope.launch {
            try {
                var total = 0.0
                var owed = 0.0
                var owedToMe = 0.0
                val groupBalMap = mutableMapOf<String, Double>()
                for (group in groups) {
                    val expenses = repository.getGroupExpenses(group.id)
                    val transactions = com.example.smartsplit.util.DebtMinimizer.minimizeDebts(expenses)
                    
                    var netForGroup = 0.0
                    for (tx in transactions) {
                        if (tx.from == currentUserId) {
                            netForGroup -= tx.amount
                            owed += tx.amount
                        }
                        if (tx.to == currentUserId) {
                            netForGroup += tx.amount
                            owedToMe += tx.amount
                        }
                    }
                    groupBalMap[group.id] = netForGroup
                    total += netForGroup
                }
                _totalBalance.value = total
                _totalOwe.value = owed
                _totalOwedToMe.value = owedToMe
                _groupBalances.value = groupBalMap
            } catch (e: Exception) {
                
            }
        }
    }
}
