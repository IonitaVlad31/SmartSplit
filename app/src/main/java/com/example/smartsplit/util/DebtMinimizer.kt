package com.example.smartsplit.util

import kotlin.math.min

import com.example.smartsplit.data.model.Expense


data class Transaction(val from: String, val to: String, val amount: Double)

object DebtMinimizer {

    
    fun minimizeDebts(expenses: List<Expense>): List<Transaction> {
        val balances = mutableMapOf<String, Double>()

        
        for (expense in expenses) {
            val splitAmount = expense.amount / expense.splitAmong.size
            
            
            balances[expense.paidBy] = (balances[expense.paidBy] ?: 0.0) + expense.amount
            
            
            for (person in expense.splitAmong) {
                balances[person] = (balances[person] ?: 0.0) - splitAmount
            }
        }

        
        
        val debtors = balances.filter { it.value < -0.01 }.mapValues { -it.value }.toMutableMap()
        val creditors = balances.filter { it.value > 0.01 }.toMutableMap()

        val transactions = mutableListOf<Transaction>()

        
        while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
            val maxDebtor = debtors.maxByOrNull { it.value }!!
            val maxCreditor = creditors.maxByOrNull { it.value }!!

            val amountToSettle = min(maxDebtor.value, maxCreditor.value)
            
            
            val roundedAmount = Math.round(amountToSettle * 100.0) / 100.0

            if (roundedAmount > 0) {
                transactions.add(Transaction(from = maxDebtor.key, to = maxCreditor.key, amount = roundedAmount))
            }

            
            debtors[maxDebtor.key] = maxDebtor.value - amountToSettle
            creditors[maxCreditor.key] = maxCreditor.value - amountToSettle

            
            if (debtors[maxDebtor.key]!! < 0.01) debtors.remove(maxDebtor.key)
            if (creditors[maxCreditor.key]!! < 0.01) creditors.remove(maxCreditor.key)
        }

        return transactions
    }
}
