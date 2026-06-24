package com.example.smartsplit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Group(val id: String, val name: String, val membersCount: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onLogoutClick: () -> Unit = {},
    onGroupClick: (String) -> Unit = {}
) {
    // Mock data for groups
    val groups = listOf(
        Group("1", "Trip to Paris", 4),
        Group("2", "Roommates", 3),
        Group("3", "Friday Dinner", 6)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Groups") },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.Logout, contentDescription = "Log Out")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Add new group */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add Group")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(groups) { group ->
                GroupItem(
                    group = group,
                    onClick = { onGroupClick(group.id) }
                )
            }
        }
    }
}

@Composable
fun GroupItem(group: Group, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "${group.membersCount} members",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
