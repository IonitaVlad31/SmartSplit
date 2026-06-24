package com.example.smartsplit.data.model

data class ChatRoom(
    val id: String = "",
    val name: String = "", // Optional: for group chats
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val unreadCount: Map<String, Int> = emptyMap() // Map de userId la unreadCount
)
