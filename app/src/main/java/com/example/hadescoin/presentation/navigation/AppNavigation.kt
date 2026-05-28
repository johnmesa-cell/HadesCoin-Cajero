package com.example.hadescoin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hadescoin.presentation.auth.login.LoginView
import com.example.hadescoin.presentation.auth.register.RegisterView
import com.example.hadescoin.presentation.home.HomeView
import com.example.hadescoin.presentation.transfer.TransferView

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginView(navController = navController)
        }

        composable("register") {
            RegisterView(navController = navController)
        }

        composable(
            route = "home/{phoneNumber}",
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            HomeView(phoneNumber = phoneNumber, navController = navController)
        }

        composable(
            route = "transfer/{senderPhone}",
            arguments = listOf(navArgument("senderPhone") { type = NavType.StringType })
        ) { backStackEntry ->
            val senderPhone = backStackEntry.arguments?.getString("senderPhone") ?: ""
            TransferView(senderPhone = senderPhone, navController = navController)
        }
    }
}
