package com.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financeapp.backup.BackupManagerViewModel
import com.financeapp.ui.navigation.FinanceAppNavigation
import com.financeapp.ui.screens.alerts.AlertsViewModel
import com.financeapp.ui.screens.calculator.CalculatorViewModel
import com.financeapp.ui.screens.dashboard.DashboardViewModel
import com.financeapp.ui.screens.expense.ExpenseViewModel
import com.financeapp.ui.screens.goals.GoalDashboardViewModel
import com.financeapp.ui.screens.goals.GoalPlannerViewModel
import com.financeapp.ui.screens.portfolio.PortfolioViewModel
import com.financeapp.ui.screens.reports.ReportsViewModel
import com.financeapp.ui.screens.salary.SalaryPlannerViewModel
import com.financeapp.ui.screens.savings.SavingsViewModel
import com.financeapp.ui.theme.FinanceAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceAppTheme {
                val dashboardViewModel: DashboardViewModel = viewModel()
                val expenseViewModel: ExpenseViewModel = viewModel()
                val alertsViewModel: AlertsViewModel = viewModel()
                val reportsViewModel: ReportsViewModel = viewModel()
                val salaryPlannerViewModel: SalaryPlannerViewModel = viewModel()
                val savingsViewModel: SavingsViewModel = viewModel()
                val goalDashboardViewModel: GoalDashboardViewModel = viewModel()
                val goalPlannerViewModel: GoalPlannerViewModel = viewModel()
                val portfolioViewModel: PortfolioViewModel = viewModel()
                val calculatorViewModel: CalculatorViewModel = viewModel()
                val backupManagerViewModel: BackupManagerViewModel = viewModel()

                FinanceAppNavigation(
                    dashboardViewModel = dashboardViewModel,
                    expenseViewModel = expenseViewModel,
                    alertsViewModel = alertsViewModel,
                    reportsViewModel = reportsViewModel,
                    salaryPlannerViewModel = salaryPlannerViewModel,
                    savingsViewModel = savingsViewModel,
                    goalDashboardViewModel = goalDashboardViewModel,
                    goalPlannerViewModel = goalPlannerViewModel,
                    portfolioViewModel = portfolioViewModel,
                    calculatorViewModel = calculatorViewModel,
                    backupManagerViewModel = backupManagerViewModel
                )
            }
        }
    }
}
