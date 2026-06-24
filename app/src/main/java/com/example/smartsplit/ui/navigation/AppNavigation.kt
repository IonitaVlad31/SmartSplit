package com.example.smartsplit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.navigation.compose.rememberNavController
import com.example.smartsplit.ui.screens.GroupDetailsScreen
import com.example.smartsplit.ui.screens.GroupsScreen
import com.example.smartsplit.ui.screens.LogInScreen
import com.example.smartsplit.ui.screens.RegisterScreen
import com.example.smartsplit.viewModels.AuthViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (authState.isAuthenticated) "main" else "login",
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable("login") {
            LogInScreen(
                onRegisterClick = { navController.navigate("register") },
                onLoginClick = { email, password -> authViewModel.login(email, password) },
                onLoginSuccess = { 
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage,
                isAuthenticated = authState.isAuthenticated
            )
        }
        composable("register") {
            RegisterScreen(
                onLoginClick = { navController.popBackStack() },
                onRegisterClick = { email, password -> authViewModel.register(email, password) },
                onRegisterSuccess = { 
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                isLoading = authState.isLoading,
                isAuthenticated = authState.isAuthenticated
            )
        }
        composable("main") {
            com.example.smartsplit.ui.screens.MainScreen(
                parentNavController = navController,
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
        composable("groupDetails/{groupId}") { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailsScreen(
                groupId = groupId,
                onBackClick = { navController.popBackStack() },
                onScanClick = { navController.navigate("scanReceipt/$groupId") }
            )
        }
        composable("chat/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            com.example.smartsplit.ui.screens.ChatScreen(
                chatId = chatId,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("scanReceipt/{groupId}") { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            com.example.smartsplit.ui.screens.ScanReceiptScreen(
                onBackClick = { navController.popBackStack() },
                onAmountScanned = { amount ->
                    navController.popBackStack()
                }
            )
        }
    }
}
