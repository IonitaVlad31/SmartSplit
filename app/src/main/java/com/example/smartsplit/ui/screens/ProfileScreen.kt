package com.example.smartsplit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.ChevronRight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartsplit.viewModels.ProfileViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.smartsplit.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    onCurrenciesClick: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel(),
    groupsViewModel: com.example.smartsplit.viewModels.GroupsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    
    val totalOwe by groupsViewModel.totalOwe.collectAsState()
    val totalOwedToMe by groupsViewModel.totalOwedToMe.collectAsState()
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                groupsViewModel.refreshBalances()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showPlaceholderDialog by remember { mutableStateOf<String?>(null) }

    
    if (showPlaceholderDialog != null) {
        AlertDialog(
            onDismissRequest = { showPlaceholderDialog = null },
            title = { Text(showPlaceholderDialog!!) },
            text = { Text("This feature will be available in the final version of the project!") },
            confirmButton = {
                TextButton(onClick = { showPlaceholderDialog = null }) {
                    Text("OK")
                }
            }
        )
    }

    
    if (showEditDialog && state.user != null) {
        var editName by remember { mutableStateOf(state.user!!.name) }
        var editHandle by remember { mutableStateOf(state.user!!.handle) }

        AlertDialog(
            onDismissRequest = { 
                showEditDialog = false
                viewModel.clearError()
            },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name / Full name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editHandle,
                        onValueChange = { editHandle = it },
                        label = { Text("Handle (e.g. name123)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        prefix = { Text("@") }
                    )
                    if (state.saveError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.saveError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.updateProfile(editName, editHandle) {
                            showEditDialog = false
                        }
                    },
                    enabled = !state.isSaving
                ) {
                    Text(if (state.isSaving) "Saving..." else "Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showEditDialog = false
                        viewModel.clearError()
                    },
                    enabled = !state.isSaving
                ) {
                    Text("Cancel")
                }
            }
        )
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
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = {  }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
            }
        }
        
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }
        
        val user = state.user
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            radius = 200f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF2B2B2B), Color(0xFF1A1A1A))
                            )
                        )
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF101010)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_clean),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize().padding(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = user?.name?.takeIf { it.isNotBlank() } ?: "Unnamed User",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = if (!user?.handle.isNullOrBlank()) "@${user?.handle}" else "@add_handle",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(modifier = Modifier.weight(1f), title = "You Owe", amount = String.format("%.2f LEI", totalOwe), color = MaterialTheme.colorScheme.error)
                StatCard(modifier = Modifier.weight(1f), title = "You Are Owed", amount = String.format("%.2f LEI", totalOwedToMe), color = Color(0xFF34C759))
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            MenuButton(icon = Icons.Default.Edit, text = "Edit Profile") { 
                showEditDialog = true 
            }
            Spacer(modifier = Modifier.height(16.dp))
            MenuButton(icon = Icons.Default.Payment, text = "Payment Methods") { 
                showPlaceholderDialog = "Payment Methods"
            }
            Spacer(modifier = Modifier.height(16.dp))
            MenuButton(icon = Icons.Default.Add, text = "Exchange Rates") { 
                onCurrenciesClick()
            }
            Spacer(modifier = Modifier.height(16.dp))
            MenuSwitch(
                icon = Icons.Default.Notifications,
                text = "Notifications",
                checked = notificationsEnabled,
                onCheckedChange = { viewModel.toggleNotifications(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            MenuButton(icon = Icons.Default.Security, text = "Security & Privacy") { 
                showPlaceholderDialog = "Security & Privacy"
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, amount: String, color: Color) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun MenuButton(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun MenuSwitch(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White, modifier = Modifier.weight(1f))
            Switch(
                checked = checked, 
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color(0xFF2A2A2A)
                )
            )
        }
    }
}
