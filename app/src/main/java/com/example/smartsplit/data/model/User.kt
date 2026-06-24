package com.example.smartsplit.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val handle: String = "",
    val friendIds: List<String> = emptyList()
)
