package com.example.smartsplit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartsplit.data.model.ChatRoom
import com.example.smartsplit.viewModels.InboxViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onChatClick: (String) -> Unit,
    viewModel: InboxViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        Text(
            text = "Messages",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)
        )
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search messages...") },
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
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else if (state.chatRooms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Nu ai niciun mesaj", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                val filteredChats = state.chatRooms.filter { room ->
                    val otherUserId = room.participantIds.find { it != state.currentUserId }
                    val otherUser = state.otherUsers[otherUserId]
                    val chatName = if (room.name.isNotBlank()) room.name else otherUser?.name ?: ""
                    val chatHandle = otherUser?.handle ?: ""
                    chatName.contains(searchQuery, ignoreCase = true) || 
                    chatHandle.contains(searchQuery, ignoreCase = true) || 
                    (room.name.isEmpty() && "Chat".contains(searchQuery, ignoreCase = true))
                }
                items(filteredChats) { room ->
                    val otherUserId = room.participantIds.find { it != state.currentUserId }
                    val otherUser = state.otherUsers[otherUserId]
                    ChatListItem(
                        room = room, 
                        currentUserId = state.currentUserId,
                        otherUser = otherUser,
                        onClick = { onChatClick(room.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatListItem(room: ChatRoom, currentUserId: String, otherUser: com.example.smartsplit.data.model.User?, onClick: () -> Unit) {
    val displayName = if (room.name.isNotBlank()) room.name 
                      else otherUser?.let { "${it.name} (@${it.handle})" } ?: "Chat Privat"
    val displayInitial = if (room.name.isNotBlank()) room.name.firstOrNull()?.toString() ?: "C"
                         else otherUser?.name?.firstOrNull()?.toString() ?: "C"
    
    // Formatting timestamp
    val timeString = if (room.lastTimestamp > 0) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(room.lastTimestamp))
    } else {
        ""
    }
    
    val unreadCount = room.unreadCount[currentUserId] ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayInitial.uppercase(),
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayName, 
                    fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = timeString, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = room.lastMessage.takeIf { it.isNotEmpty() } ?: "Fără mesaje", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (unreadCount > 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
