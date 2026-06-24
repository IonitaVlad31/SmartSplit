package com.example.smartsplit.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class BottomNavItem(var title: String, var icon: ImageVector, var route: String) {
    object Home : BottomNavItem("Home", Icons.Default.Home, "home_tab")
    object Friends : BottomNavItem("Friends", Icons.Default.People, "friends_tab")
    object Add : BottomNavItem("", Icons.Default.Add, "add_tab") // Empty title for central prominent button
    object Inbox : BottomNavItem("Inbox", Icons.Default.Chat, "inbox_tab")
    object Profile : BottomNavItem("Profile", Icons.Default.Person, "profile_tab")
}

@Composable
fun MainScreen(
    parentNavController: NavController,
    onLogoutClick: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) },
        content = { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavigationGraph(navController = navController, parentNavController = parentNavController, onLogoutClick = onLogoutClick)
            }
        }
    )
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Friends,
        BottomNavItem.Add,
        BottomNavItem.Inbox,
        BottomNavItem.Profile
    )
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val isSelected = currentRoute == item.route
            
            if (item == BottomNavItem.Add) {
                // The central 'Add' button
                NavigationBarItem(
                    icon = {
                        FloatingActionButton(
                            onClick = {
                                navController.navigate(item.route) {
                                    navController.graph.startDestinationRoute?.let { route ->
                                        popUpTo(route) { saveState = true }
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(item.icon, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    selected = false, // Always keep it as an action button style
                    onClick = { /* Handled by FAB onClick */ },
                    alwaysShowLabel = false
                )
            } else {
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(item.title, style = MaterialTheme.typography.labelSmall) },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) { saveState = true }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController, 
    parentNavController: NavController,
    onLogoutClick: () -> Unit
) {
    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) {
            GroupsScreen(
                onGroupClick = { groupId ->
                    // Navigate to group details via the PARENT navController
                    // because group details is a full-screen feature, hiding the bottom bar
                    parentNavController.navigate("groupDetails/$groupId")
                }
            )
        }
        composable(BottomNavItem.Friends.route) {
            FriendsScreen()
        }
        composable(BottomNavItem.Add.route) {
            // Temporary screen or could just open a bottom sheet modal
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Add New Group / Expense Overlay")
            }
        }
        composable(BottomNavItem.Inbox.route) {
            InboxScreen(
                onChatClick = { chatId ->
                    parentNavController.navigate("chat/$chatId")
                }
            )
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(onLogoutClick = onLogoutClick)
        }
    }
}
