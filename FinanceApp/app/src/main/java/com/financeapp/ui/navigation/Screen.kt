package com.financeapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Home", Icons.Default.Dashboard)
    data object Expenses : Screen("expenses", "Expenses", Icons.Default.PieChart)
    data object Goals : Screen("goals", "Goals", Icons.Default.Flag)
    data object Reports : Screen("reports", "Reports", Icons.Default.BarChart)
    data object More : Screen("more", "More", Icons.Default.MoreHoriz)

    // Sub-screens (not in bottom nav)
    data object Alerts : Screen("alerts", "Alerts", Icons.Default.Notifications)
    data object Salary : Screen("salary", "Salary", Icons.Default.BarChart)
    data object GoalPlanner : Screen("goal_planner", "Planner", Icons.Default.BarChart)
    data object Savings : Screen("savings", "Savings", Icons.Default.BarChart)
    data object Portfolio : Screen("portfolio", "Portfolio", Icons.Default.BarChart)
    data object Calculator : Screen("calculator", "Calculator", Icons.Default.Calculate)
    data object Backup : Screen("backup", "Backup", Icons.Default.Backup)

    companion object {
        val bottomNavScreens = listOf(Dashboard, Expenses, Goals, Reports, More)
        val allScreens = listOf(Dashboard, Expenses, Goals, Reports, More, Alerts, Salary, GoalPlanner, Savings, Portfolio, Calculator, Backup)
    }
}
