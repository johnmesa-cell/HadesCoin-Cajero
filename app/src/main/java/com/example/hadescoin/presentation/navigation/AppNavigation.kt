package com.example.hadescoin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hadescoin.presentation.atm.AtmOperation
import com.example.hadescoin.presentation.atm.AtmView
import com.example.hadescoin.presentation.home.HomeView

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeView(navController = navController)
        }

        composable(
            route     = "atm/{operation}",
            arguments = listOf(
                navArgument("operation") { type = NavType.StringType }
            )
        ) { back ->
            val opStr = back.arguments?.getString("operation") ?: "DEPOSIT"
            val op = try {
                AtmOperation.valueOf(opStr)
            } catch (_: Exception) {
                AtmOperation.DEPOSIT
            }
            AtmView(
                operation     = op,
                navController = navController
            )
        }
    }
}
