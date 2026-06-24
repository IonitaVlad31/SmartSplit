package com.example.smartsplit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartsplit.data.model.User
import com.example.smartsplit.viewModels.FriendsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onChatClick: (String) -> Unit,
    viewModel: FriendsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newFriendHandle by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                viewModel.resetAddFriendState() 
            },
            title = { Text("Adaugă Prieten") },
            text = {
                Column {
                    Text("Introdu handle-ul (ex: @mihai)")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFriendHandle,
                        onValueChange = { newFriendHandle = it },
                        singleLine = true,
                        placeholder = { Text("@handle") }
                    )
                    if (state.error != null && state.isAddingFriend.not()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                    }
                    if (state.addFriendSuccess) {
                        LaunchedEffect(Unit) {
                            showAddDialog = false
                            viewModel.resetAddFriendState()
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.addFriendByHandle(newFriendHandle) },
                    enabled = !state.isAddingFriend
                ) {
                    if (state.isAddingFriend) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Adaugă")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    viewModel.resetAddFriendState()
                }) {
                    Text("Anulează")
                }
            }
        )
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(top = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = { 
                        newFriendHandle = ""
                        showAddDialog = true 
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Friend", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search friends...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.friends.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Nu ai niciun prieten adăugat", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val filteredFriends = state.friends.filter { 
                        it.name.contains(searchQuery, ignoreCase = true) || it.handle.contains(searchQuery, ignoreCase = true)
                    }
                    items(filteredFriends) { friend ->
                        FriendItem(
                            friend = friend,
                            onMessageClick = {
                                coroutineScope.launch {
                                    val chatId = viewModel.getChatRoomWith(friend.id)
                                    if (chatId != null) {
                                        onChatClick(chatId)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendItem(friend: User, onMessageClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.name.firstOrNull()?.toString()?.uppercase() ?: "F",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = friend.name.ifEmpty { "Fără Nume" }, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                val displayHandle = if (friend.handle.startsWith("@")) friend.handle else "@${friend.handle}"
                Text(text = displayHandle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            IconButton(
                onClick = onMessageClick,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
            ) {
                Icon(
                    Icons.Default.Chat, 
                    contentDescription = "Message",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = { /* Split */ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text("Split", fontWeight = FontWeight.Bold)
            }
        }
    }
}
