package com.example.smartsplit.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsplit.data.model.Expense
import com.example.smartsplit.data.model.Group
import com.example.smartsplit.data.model.User
import com.example.smartsplit.data.repository.FirebaseRepository
import com.example.smartsplit.util.DebtMinimizer
import com.example.smartsplit.util.Transaction
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class GroupDetailsState(
    val isLoading: Boolean = true,
    val group: Group? = null,
    val expenses: List<Expense> = emptyList(),
    val members: Map<String, User> = emptyMap(),
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null
)

class GroupDetailsViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(GroupDetailsState())
    val state: StateFlow<GroupDetailsState> = _state.asStateFlow()

    private var groupId: String? = null
    private var hasInitialized = false

    fun initGroup(id: String) {
        if (hasInitialized && groupId == id) return
        hasInitialized = true
        groupId = id

        observeGroupData(id)
        observeExpenses(id)
    }

    private fun observeGroupData(id: String) {
        viewModelScope.launch {
            repository.observeGroup(id)
                .catch { e ->
                    _state.value = _state.value.copy(error = e.message, isLoading = false)
                }
                .collect { group ->
                    if (group != null) {
                        _state.value = _state.value.copy(group = group)
                        fetchMembers(group.memberIds)
                    } else {
                        _state.value = _state.value.copy(error = "Group not found", isLoading = false)
                    }
                }
        }
    }

    private fun fetchMembers(memberIds: List<String>) {
        viewModelScope.launch {
            try {
                val memberMap = mutableMapOf<String, User>()
                for (id in memberIds) {
                    val user = repository.getUser(id)
                    if (user != null) {
                        memberMap[id] = user
                    }
                }
                _state.value = _state.value.copy(members = memberMap, isLoading = false)
            } catch (e: Exception) {
                Log.e("GroupDetailsVM", "Error fetching members", e)
            }
        }
    }

    private fun observeExpenses(id: String) {
        viewModelScope.launch {
            repository.observeGroupExpenses(id)
                .catch { e ->
                    Log.e("GroupDetailsVM", "Error observing expenses", e)
                }
                .collect { expenses ->
                    val transactions = DebtMinimizer.minimizeDebts(expenses)
                    _state.value = _state.value.copy(expenses = expenses, transactions = transactions)
                }
        }
    }

    fun addExpense(amount: Double, description: String) {
        val currentGroupId = groupId ?: return
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val currentGroup = _state.value.group ?: return

        
        val splitAmong = currentGroup.memberIds
        if (splitAmong.isEmpty()) return

        viewModelScope.launch {
            try {
                val expense = Expense(
                    id = UUID.randomUUID().toString(),
                    groupId = currentGroupId,
                    paidBy = currentUser.uid,
                    amount = amount,
                    description = description,
                    splitAmong = splitAmong,
                    timestamp = System.currentTimeMillis()
                )
                repository.addExpense(expense)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Failed to add expense: ${e.message}")
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
