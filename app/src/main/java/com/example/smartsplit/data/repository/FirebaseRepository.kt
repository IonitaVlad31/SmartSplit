package com.example.smartsplit.data.repository

import com.example.smartsplit.data.model.Expense
import com.example.smartsplit.data.model.Group
import com.example.smartsplit.data.model.Message
import com.example.smartsplit.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveUser(user: User) {
        db.collection("users").document(user.id).set(user).await()
    }

    suspend fun getUser(userId: String): User? {
        val document = db.collection("users").document(userId).get().await()
        return document.toObject(User::class.java)
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>) {
        db.collection("users").document(userId).update(updates).await()
    }

    suspend fun isHandleTaken(handle: String, excludeUserId: String): Boolean {
        if (handle.isBlank()) return false
        val snapshot = db.collection("users")
            .whereEqualTo("handle", handle)
            .get()
            .await()
        return snapshot.documents.any { it.id != excludeUserId }
    }

    suspend fun createGroup(group: Group) {
        // If the ID is empty, we generate a new document reference
        val docRef = if (group.id.isEmpty()) db.collection("groups").document() else db.collection("groups").document(group.id)
        val groupWithId = group.copy(id = docRef.id)
        docRef.set(groupWithId).await()
    }

    suspend fun getUserGroups(userId: String): List<Group> {
        val snapshot = db.collection("groups")
            .whereArrayContains("memberIds", userId)
            .get()
            .await()
        return snapshot.toObjects(Group::class.java)
    }

    suspend fun addExpense(expense: Expense) {
        val docRef = if (expense.id.isEmpty()) db.collection("expenses").document() else db.collection("expenses").document(expense.id)
        val expenseWithId = expense.copy(id = docRef.id)
        docRef.set(expenseWithId).await()
    }

    suspend fun getGroupExpenses(groupId: String): List<Expense> {
        val snapshot = db.collection("expenses")
            .whereEqualTo("groupId", groupId)
            .get()
            .await()
        return snapshot.toObjects(Expense::class.java)
    }

    suspend fun sendMessage(message: Message) {
        val docRef = if (message.id.isEmpty()) db.collection("messages").document() else db.collection("messages").document(message.id)
        val msgWithId = message.copy(id = docRef.id)
        docRef.set(msgWithId).await()
    }
}
