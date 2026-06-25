package com.example.smartsplit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.Add
import com.example.smartsplit.viewModels.GroupsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    viewModel: GroupsViewModel,
    onLogoutClick: () -> Unit = {},
    onGroupClick: (String) -> Unit = {}
) {
    val groups by viewModel.groups.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val groupBalances by viewModel.groupBalances.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshBalances()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Group", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                val prefix = if (totalBalance > 0) "+" else ""
                val color = if (totalBalance < 0) Color(0xFFFFCCCC) else MaterialTheme.colorScheme.onPrimary
                
                Text(
                    text = "$prefix${String.format("%.2f", totalBalance)} LEI",
                    style = MaterialTheme.typography.displaySmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Active Splits",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp) // padding for bottom bar
        ) {
            if (groups.isEmpty() && !isLoading) {
                item {
                    Text(
                        text = "No groups yet. Create one!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(groups) { group ->
                    val balance = groupBalances[group.id] ?: 0.0
                    GroupItem(
                        group = group,
                        balance = balance,
                        onClick = { onGroupClick(group.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        var groupName by remember { mutableStateOf("") }
        val selectedFriendIds = remember { androidx.compose.runtime.mutableStateListOf<String>() }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Group") },
            text = {
                Column {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (friends.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Add Friends:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(friends) { friend ->
                                val isSelected = selectedFriendIds.contains(friend.id)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isSelected) {
                                                selectedFriendIds.remove(friend.id)
                                            } else {
                                                selectedFriendIds.add(friend.id)
                                            }
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                selectedFriendIds.add(friend.id)
                                            } else {
                                                selectedFriendIds.remove(friend.id)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(friend.name)
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "You don't have any friends added yet. You can add them from the Friends tab.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            viewModel.createGroup(name = groupName, memberIds = selectedFriendIds.toList())
                        }
                        showCreateDialog = false
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

}

@Composable
fun GroupItem(group: com.example.smartsplit.data.model.Group, balance: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home, // Using a default icon for all real groups for now
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${group.memberIds.size} members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val color = when {
                balance > 0 -> Color(0xFF34C759)
                balance < 0 -> Color(0xFFFF3B30)
                else -> MaterialTheme.colorScheme.onSurface
            }
            val prefix = if (balance > 0) "+" else ""

            Text(
                text = "$prefix${String.format("%.2f", balance)} LEI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
