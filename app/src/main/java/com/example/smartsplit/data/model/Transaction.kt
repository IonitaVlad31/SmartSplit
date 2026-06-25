package com.example.smartsplit.data.model

/**
 * Represents a debt that needs to be paid.
 * [from] owes [to] an [amount] of money.
 */
data class Transaction(
    val from: String = "",
    val to: String = "",
    val amount: Double = 0.0
)
