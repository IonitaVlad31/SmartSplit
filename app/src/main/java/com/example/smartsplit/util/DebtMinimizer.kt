package com.example.smartsplit.util

import kotlin.math.min

/**
 * Represents a debt that needs to be paid.
 * [from] owes [to] an [amount] of money.
 */
data class Transaction(val from: String, val to: String, val amount: Double)

/**
 * An expense paid by [payerId] for a total [amount].
 * The cost is split equally among all [involvedIds].
 */
data class Expense(val payerId: String, val amount: Double, val involvedIds: List<String>)

object DebtMinimizer {

    /**
     * Calculates the minimum number of transactions needed to settle all debts.
     * Uses a greedy algorithm on the net balances of all members.
     */
    fun minimizeDebts(expenses: List<Expense>): List<Transaction> {
        val balances = mutableMapOf<String, Double>()

        // 1. Calculate net balance for each person
        for (expense in expenses) {
            val splitAmount = expense.amount / expense.involvedIds.size
            
            // Payer gets back the total amount they paid
            balances[expense.payerId] = (balances[expense.payerId] ?: 0.0) + expense.amount
            
            // Every involved person owes their share
            for (person in expense.involvedIds) {
                balances[person] = (balances[person] ?: 0.0) - splitAmount
            }
        }

        // 2. Separate into debtors (negative balance) and creditors (positive balance)
        // We round to 2 decimals to avoid floating point precision issues
        val debtors = balances.filter { it.value < -0.01 }.mapValues { -it.value }.toMutableMap()
        val creditors = balances.filter { it.value > 0.01 }.toMutableMap()

        val transactions = mutableListOf<Transaction>()

        // 3. Greedily match max debtor with max creditor
        while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
            val maxDebtor = debtors.maxByOrNull { it.value }!!
            val maxCreditor = creditors.maxByOrNull { it.value }!!

            val amountToSettle = min(maxDebtor.value, maxCreditor.value)
            
            // Round to 2 decimal places
            val roundedAmount = Math.round(amountToSettle * 100.0) / 100.0

            if (roundedAmount > 0) {
                transactions.add(Transaction(from = maxDebtor.key, to = maxCreditor.key, amount = roundedAmount))
            }

            // Update balances
            debtors[maxDebtor.key] = maxDebtor.value - amountToSettle
            creditors[maxCreditor.key] = maxCreditor.value - amountToSettle

            // Remove settled individuals
            if (debtors[maxDebtor.key]!! < 0.01) debtors.remove(maxDebtor.key)
            if (creditors[maxCreditor.key]!! < 0.01) creditors.remove(maxCreditor.key)
        }

        return transactions
    }
}
