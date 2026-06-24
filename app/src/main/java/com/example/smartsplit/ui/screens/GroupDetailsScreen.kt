package com.example.smartsplit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartsplit.util.DebtMinimizer
import com.example.smartsplit.util.Expense
import com.example.smartsplit.util.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    onBackClick: () -> Unit = {},
    onScanClick: () -> Unit = {}
) {
    // Mock data
    val members = listOf("Alice", "Bob", "Charlie", "David")
    
    var expenses by remember { 
        mutableStateOf(listOf(
            Expense("Alice", 120.0, members),
            Expense("Bob", 40.0, listOf("Bob", "Charlie", "David")),
            Expense("Charlie", 30.0, listOf("Alice", "Charlie"))
        ))
    }
    
    val transactions = remember(expenses) { DebtMinimizer.minimizeDebts(expenses) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip to Paris") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = { onScanClick() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, "Scan Receipt")
                }
                FloatingActionButton(
                    onClick = { 
                        // Add mock expense for testing
                        expenses = expenses + Expense("David", 50.0, members)
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Add Expense")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Balances / Settle Up Section
            item {
                Text(
                    text = "How to Settle Up",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                if (transactions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            text = "Everyone is settled up!",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                } else {
                    transactions.forEach { tx ->
                        TransactionItem(tx)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Expenses Section
            item {
                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(expenses.reversed()) { expense ->
                ExpenseItem(expense)
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = transaction.from, fontWeight = FontWeight.Bold)
                    Text(text = "Owes", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "To", tint = MaterialTheme.colorScheme.tertiary)
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${transaction.amount} LEI", 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = "to ${transaction.to}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = expense.payerId, fontWeight = FontWeight.Bold)
                    Text(
                        text = "paid for ${expense.involvedIds.size} people",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "${expense.amount} LEI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
