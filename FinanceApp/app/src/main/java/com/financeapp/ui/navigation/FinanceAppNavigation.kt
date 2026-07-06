package com.financeapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.financeapp.backup.BackupManagerScreen
import com.financeapp.backup.BackupManagerViewModel
import com.financeapp.ui.screens.alerts.AlertsScreen
import com.financeapp.ui.screens.alerts.AlertsViewModel
import com.financeapp.ui.screens.calculator.CalculatorScreen
import com.financeapp.ui.screens.calculator.CalculatorViewModel
import com.financeapp.ui.screens.dashboard.DashboardScreen
import com.financeapp.ui.screens.dashboard.DashboardViewModel
import com.financeapp.ui.screens.expense.ExpenseScreen
import com.financeapp.ui.screens.expense.ExpenseViewModel
import com.financeapp.ui.screens.goals.GoalDashboardScreen
import com.financeapp.ui.screens.goals.GoalDashboardViewModel
import com.financeapp.ui.screens.goals.GoalPlannerScreen
import com.financeapp.ui.screens.goals.GoalPlannerViewModel
import com.financeapp.ui.screens.portfolio.PortfolioScreen
import com.financeapp.ui.screens.portfolio.PortfolioViewModel
import com.financeapp.ui.screens.reports.ReportsScreen
import com.financeapp.ui.screens.reports.ReportsViewModel
import com.financeapp.ui.screens.salary.SalaryPlannerScreen
import com.financeapp.ui.screens.salary.SalaryPlannerViewModel
import com.financeapp.ui.screens.savings.SavingsScreen
import com.financeapp.ui.screens.savings.SavingsViewModel

@Composable
fun FinanceAppNavigation(
    dashboardViewModel: DashboardViewModel,
    expenseViewModel: ExpenseViewModel,
    alertsViewModel: AlertsViewModel,
    reportsViewModel: ReportsViewModel,
    salaryPlannerViewModel: SalaryPlannerViewModel,
    savingsViewModel: SavingsViewModel,
    goalDashboardViewModel: GoalDashboardViewModel,
    goalPlannerViewModel: GoalPlannerViewModel,
    portfolioViewModel: PortfolioViewModel,
    calculatorViewModel: CalculatorViewModel,
    backupManagerViewModel: BackupManagerViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = dashboardViewModel)
            }
            composable(Screen.Expenses.route) {
                ExpenseScreen(viewModel = expenseViewModel)
            }
            composable(Screen.Alerts.route) {
                AlertsScreen(viewModel = alertsViewModel)
            }
            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = reportsViewModel)
            }
            composable(Screen.Salary.route) {
                SalaryPlannerScreen(viewModel = salaryPlannerViewModel)
            }
            composable(Screen.Goals.route) {
                GoalDashboardScreen(viewModel = goalDashboardViewModel)
            }
            composable(Screen.GoalPlanner.route) {
                GoalPlannerScreen(viewModel = goalPlannerViewModel)
            }
            composable(Screen.Savings.route) {
                SavingsScreen(viewModel = savingsViewModel)
            }
            composable(Screen.Portfolio.route) {
                PortfolioScreen(viewModel = portfolioViewModel)
            }
            composable(Screen.Calculator.route) {
                CalculatorScreen(viewModel = calculatorViewModel)
            }
            composable(Screen.Backup.route) {
                BackupManagerScreen(viewModel = backupManagerViewModel)
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        Screen.allScreens.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = {
                    Text(
                        screen.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
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
