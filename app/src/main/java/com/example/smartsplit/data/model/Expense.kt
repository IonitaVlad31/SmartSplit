package com.example.smartsplit.data.model

data class Expense(
    val id: String = "",
    val groupId: String = "",
    val paidBy: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val splitAmong: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
