package com.example.smartexpense.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartexpense.ui.add_edit.AddEditTransactionScreen
import com.example.smartexpense.ui.all_transactions.AllTransactionsScreen
import com.example.smartexpense.ui.analysis.AnalysisDashboardScreen
import com.example.smartexpense.ui.dashboard.DashboardScreen
import com.example.smartexpense.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AllTransactions : Screen("all_transactions")
    object Analysis : Screen("analysis")
    object AddEditTransaction : Screen("add_edit_transaction?transactionId={transactionId}") {
        fun createRoute(transactionId: Long? = null) = "add_edit_transaction?transactionId=${transactionId ?: -1L}"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        Triple(Screen.Dashboard.route, "Home", Icons.Default.Home),
        Triple(Screen.Analysis.route, "Analysis", Icons.Default.BarChart),
        Triple(Screen.Settings.route, "Settings", Icons.Default.Settings)
    )

    val showBottomBar = bottomNavItems.any { it.first == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { (route, label, icon) ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == route } == true
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onAddTransactionClick = {
                        navController.navigate(Screen.AddEditTransaction.createRoute())
                    },
                    onTransactionClick = { id ->
                        navController.navigate(Screen.AddEditTransaction.createRoute(id))
                    },
                    onSettingsClick = {  },
                    onViewAllClick = {
                        navController.navigate(Screen.AllTransactions.route)
                    },
                    onAnalysisClick = {  }
                )
            }

            composable(
                route = Screen.AddEditTransaction.route,
                arguments = listOf(
                    navArgument("transactionId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                AddEditTransactionScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.AllTransactions.route) {
                AllTransactionsScreen(
                    onBackClick = { navController.popBackStack() },
                    onTransactionClick = { id ->
                        navController.navigate(Screen.AddEditTransaction.createRoute(id))
                    }
                )
            }

            composable(Screen.Analysis.route) {
                AnalysisDashboardScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
