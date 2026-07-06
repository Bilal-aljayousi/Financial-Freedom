package com.financeapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Home", Icons.Default.Dashboard)
    data object Expenses : Screen("expenses", "Expenses", Icons.Default.PieChart)
    data object Alerts : Screen("alerts", "Alerts", Icons.Default.Notifications)
    data object Reports : Screen("reports", "Reports", Icons.Default.BarChart)
    data object Salary : Screen("salary", "Salary", Icons.Default.BarChart)
    data object Goals : Screen("goals", "Goals", Icons.Default.Flag)
    data object GoalPlanner : Screen("goal_planner", "Planner", Icons.Default.TrackChanges)
    data object Savings : Screen("savings", "Savings", Icons.Default.Savings)
    data object Portfolio : Screen("portfolio", "Portfolio", Icons.Default.AccountBalance)
    data object Calculator : Screen("calculator", "Calculator", Icons.Default.Calculate)
    data object Backup : Screen("backup", "Backup", Icons.Default.Backup)

    companion object {
        val allScreens = listOf(Dashboard, Expenses, Alerts, Reports, Goals, GoalPlanner, Portfolio, Calculator, Backup)
    }
}
