package com.example.smartsplit.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
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
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = { BottomNavigationBar(navController = navController) },
        content = { paddingValues ->
            // Use fillMaxSize so content goes UNDER the navigation bar
            Box(modifier = Modifier.fillMaxSize()) {
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
        containerColor = androidx.compose.ui.graphics.Color(0xEE050505), // Very dark, almost black, with slight transparency
        contentColor = androidx.compose.ui.graphics.Color.White,
        modifier = Modifier.background(androidx.compose.ui.graphics.Color(0xEE050505))
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val isSelected = currentRoute == item.route
            
            if (item == BottomNavItem.Add) {
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
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Image(
                                painter = painterResource(id = com.example.smartsplit.R.drawable.logo_cropped),
                                contentDescription = "Add",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                            )
                        }
                    },
                    selected = false,
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
                        unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = androidx.compose.ui.graphics.Color.Gray,
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent
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
    NavHost(
        navController = navController, 
        startDestination = BottomNavItem.Home.route,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
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
            FriendsScreen(
                onChatClick = { chatId ->
                    parentNavController.navigate("chat/$chatId")
                }
            )
        }
        composable(BottomNavItem.Add.route) {
            ScanReceiptScreen(onBackClick = null)
        }
        composable(BottomNavItem.Inbox.route) {
            InboxScreen(
                onChatClick = { chatId ->
                    parentNavController.navigate("chat/$chatId")
                }
            )
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(
                onLogoutClick = onLogoutClick,
                onCurrenciesClick = { parentNavController.navigate("currencies") }
            )
        }
    }
}
