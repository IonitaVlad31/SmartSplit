package com.example.smartsplit.data.repository

import com.example.smartsplit.data.model.Expense
import com.example.smartsplit.data.model.Group
import com.example.smartsplit.data.model.Message
import com.example.smartsplit.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
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

    fun observeUser(userId: String): kotlinx.coroutines.flow.Flow<User?> = kotlinx.coroutines.flow.callbackFlow {
        val listener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(User::class.java))
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun isHandleTaken(handle: String, excludeUserId: String): Boolean {
        if (handle.isBlank()) return false
        val snapshot = db.collection("users")
            .whereEqualTo("handle", handle)
            .get()
            .await()
        return snapshot.documents.any { it.id != excludeUserId }
    }

    suspend fun createGroup(group: Group): String {
        
        val docRef = if (group.id.isEmpty()) db.collection("groups").document() else db.collection("groups").document(group.id)
        val groupWithId = group.copy(id = docRef.id)
        docRef.set(groupWithId).await()
        return docRef.id
    }

    fun observeGroup(groupId: String): kotlinx.coroutines.flow.Flow<Group?> = kotlinx.coroutines.flow.callbackFlow {
        val listener = db.collection("groups").document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(Group::class.java))
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeGroupExpenses(groupId: String): kotlinx.coroutines.flow.Flow<List<Expense>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = db.collection("expenses")
            .whereEqualTo("groupId", groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val expenses = snapshot.toObjects(Expense::class.java)
                    trySend(expenses.sortedByDescending { it.timestamp })
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getUserGroups(userId: String): List<Group> {
        val snapshot = db.collection("groups")
            .whereArrayContains("memberIds", userId)
            .get()
            .await()
        return snapshot.toObjects(Group::class.java)
    }

    fun observeUserGroups(userId: String): kotlinx.coroutines.flow.Flow<List<Group>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = db.collection("groups")
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val groups = snapshot.toObjects(Group::class.java)
                    trySend(groups)
                }
            }
        awaitClose { listener.remove() }
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
        
        if (msgWithId.chatId.isNotEmpty()) {
            try {
                db.collection("chatRooms").document(msgWithId.chatId).update(
                    mapOf(
                        "lastMessage" to msgWithId.text,
                        "lastTimestamp" to msgWithId.timestamp
                    )
                ).await()
            } catch (e: Exception) {
                
            }
        }
    }

    suspend fun createChatRoom(room: com.example.smartsplit.data.model.ChatRoom): String {
        val docRef = if (room.id.isEmpty()) db.collection("chatRooms").document() else db.collection("chatRooms").document(room.id)
        val roomWithId = room.copy(id = docRef.id)
        docRef.set(roomWithId).await()
        return docRef.id
    }

    fun observeUserChatRooms(userId: String): kotlinx.coroutines.flow.Flow<List<com.example.smartsplit.data.model.ChatRoom>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = db.collection("chatRooms")
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val rooms = snapshot.toObjects(com.example.smartsplit.data.model.ChatRoom::class.java)
                    trySend(rooms.sortedByDescending { it.lastTimestamp })
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeMessages(chatId: String): kotlinx.coroutines.flow.Flow<List<Message>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = db.collection("messages")
            .whereEqualTo("chatId", chatId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java).sortedByDescending { it.timestamp }
                    trySend(messages)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun searchUserByHandle(handle: String): User? {
        val cleanHandle = if (handle.startsWith("@")) handle.substring(1) else handle
        val snapshot = db.collection("users")
            .whereEqualTo("handle", cleanHandle)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.toObject(User::class.java)
    }

    suspend fun addFriend(currentUserId: String, friendId: String) {
        if (currentUserId == friendId) return
        
        val currentUserRef = db.collection("users").document(currentUserId)
        val friendUserRef = db.collection("users").document(friendId)
        
        db.runTransaction { transaction ->
            val currentUser = transaction.get(currentUserRef).toObject(User::class.java)
            val friendUser = transaction.get(friendUserRef).toObject(User::class.java)
            
            if (currentUser != null && friendUser != null) {
                val updatedCurrentFriends = currentUser.friendIds.toMutableSet().apply { add(friendId) }.toList()
                val updatedFriendFriends = friendUser.friendIds.toMutableSet().apply { add(currentUserId) }.toList()
                
                transaction.update(currentUserRef, "friendIds", updatedCurrentFriends)
                transaction.update(friendUserRef, "friendIds", updatedFriendFriends)
            }
        }.await()
    }

    suspend fun getFriends(friendIds: List<String>): List<User> {
        if (friendIds.isEmpty()) return emptyList()
        val chunks = friendIds.chunked(30)
        val friends = mutableListOf<User>()
        for (chunk in chunks) {
            val snapshot = db.collection("users")
                .whereIn("id", chunk)
                .get()
                .await()
            friends.addAll(snapshot.toObjects(User::class.java))
        }
        return friends
    }

    suspend fun getOrCreateDirectChat(userId1: String, userId2: String): String {
        val snapshot = db.collection("chatRooms")
            .whereArrayContains("participantIds", userId1)
            .get()
            .await()
        
        val existingRoom = snapshot.toObjects(com.example.smartsplit.data.model.ChatRoom::class.java)
            .find { it.participantIds.contains(userId2) && it.participantIds.size == 2 }
            
        if (existingRoom != null) {
            return existingRoom.id
        }
        
        val newRoom = com.example.smartsplit.data.model.ChatRoom(
            participantIds = listOf(userId1, userId2),
            name = "" 
        )
        return createChatRoom(newRoom)
    }

    suspend fun getOrCreateDirectGroup(userId1: String, userId2: String, friendName: String): String {
        val snapshot = db.collection("groups")
            .whereArrayContains("memberIds", userId1)
            .get()
            .await()
        
        val existingGroup = snapshot.toObjects(Group::class.java)
            .find { it.memberIds.contains(userId2) && it.memberIds.size == 2 }
            
        if (existingGroup != null) {
            return existingGroup.id
        }
        
        val newGroup = Group(
            name = "Split cu $friendName",
            memberIds = listOf(userId1, userId2)
        )
        return createGroup(newGroup)
    }
}
