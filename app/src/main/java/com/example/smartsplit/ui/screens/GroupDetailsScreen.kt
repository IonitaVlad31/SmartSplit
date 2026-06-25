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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartsplit.data.model.Expense
import com.example.smartsplit.data.model.User
import com.example.smartsplit.util.Transaction
import com.example.smartsplit.viewModels.GroupDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    viewModel: com.example.smartsplit.viewModels.GroupDetailsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onScanClick: () -> Unit,
    scannedAmount: Double? = null,
    clearScannedAmount: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(scannedAmount) {
        if (scannedAmount != null) {
            amountText = scannedAmount.toString()
            showAddExpenseDialog = true
            clearScannedAmount()
        }
    }

    if (showAddExpenseDialog) {
        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("Add Expense") },
            text = {
                Column {
                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        label = { Text("What was this for?") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (LEI)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount != null && descriptionText.isNotBlank()) {
                            viewModel.addExpense(amount, descriptionText)
                            showAddExpenseDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(state.group?.name ?: "Loading...") },
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
                    onClick = { showAddExpenseDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Add Expense")
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (state.expenses.isNotEmpty()) {
                    item {
                        AnimatedPieChart(state.expenses, state.members)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                item {
                    Text(
                        text = "How to Settle Up",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (state.transactions.isEmpty()) {
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
                        state.transactions.forEach { tx ->
                            TransactionItem(tx, state.members)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                item {
                    Text(
                        text = "Recent Expenses",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (state.expenses.isEmpty()) {
                    item {
                        Text(
                            text = "No expenses yet. Add one!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(state.expenses) { expense ->
                        ExpenseItem(expense, state.members)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, members: Map<String, User>) {
    val fromName = members[transaction.from]?.name ?: "Unknown"
    val toName = members[transaction.to]?.name ?: "Unknown"

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
                    Text(text = fromName, fontWeight = FontWeight.Bold)
                    Text(text = "Owes", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "To", tint = MaterialTheme.colorScheme.tertiary)
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.2f", transaction.amount)} LEI", 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = "to $toName", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, members: Map<String, User>) {
    val payerName = members[expense.paidBy]?.name ?: "Unknown"

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
                    Text(text = payerName, fontWeight = FontWeight.Bold)
                    Text(
                        text = "paid for ${expense.splitAmong.size} people",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (expense.description.isNotBlank()) {
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = "${String.format("%.2f", expense.amount)} LEI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AnimatedPieChart(expenses: List<Expense>, members: Map<String, User>) {
    val spendingPerPerson = expenses.groupBy { it.paidBy }
        .mapValues { it.value.sumOf { exp -> exp.amount } }
    
    val totalSpending = spendingPerPerson.values.sum()
    if (totalSpending <= 0.0) return

    val angles = spendingPerPerson.values.map { (it / totalSpending).toFloat() * 360f }
    val colors = listOf(
        androidx.compose.ui.graphics.Color(0xFFFF3B30), // Vibrant Red
        androidx.compose.ui.graphics.Color(0xFF34C759), // Vibrant Green
        androidx.compose.ui.graphics.Color(0xFF007AFF), // Vibrant Blue
        androidx.compose.ui.graphics.Color(0xFFFF9500), // Vibrant Orange
        androidx.compose.ui.graphics.Color(0xFFAF52DE)  // Vibrant Purple
    )

    val animationProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    
    LaunchedEffect(expenses) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 40.dp.toPx()
                var startAngle = -90f
                
                angles.forEachIndexed { index, angle ->
                    val sweepAngle = angle * animationProgress.value
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Text(
                    text = "${totalSpending.toInt()} LEI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            spendingPerPerson.keys.forEachIndexed { index, payerId ->
                val payerName = members[payerId]?.name ?: "Unknown"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(colors[index % colors.size])
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = payerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
