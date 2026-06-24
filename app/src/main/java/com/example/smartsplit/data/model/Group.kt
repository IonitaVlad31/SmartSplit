package com.example.smartsplit.data.model

data class Group(
    val id: String = "",
    val name: String = "",
    val memberIds: List<String> = emptyList(),
    val balances: Map<String, Double> = emptyMap()
)
