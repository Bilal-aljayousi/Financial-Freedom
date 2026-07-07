package com.financeapp.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financeapp.ui.components.PieChart
import com.financeapp.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToGoalPlanner: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {}
) {
    val summary by viewModel.summary.collectAsState()
    val categoryTotals by viewModel.categoryTotals.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Freedom") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick Access Buttons
            item {
                Text(
                    "Quick Access",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAccessButton(
                        icon = Icons.Default.PieChart,
                        label = "Expenses",
                        color = Color(0xFFF44336),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToExpenses
                    )
                    QuickAccessButton(
                        icon = Icons.Default.Flag,
                        label = "Goals",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToGoals
                    )
                    QuickAccessButton(
                        icon = Icons.Default.TrackChanges,
                        label = "Planner",
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToGoalPlanner
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAccessButton(
                        icon = Icons.Default.BarChart,
                        label = "Reports",
                        color = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReports
                    )
                    QuickAccessButton(
                        icon = Icons.Default.Notifications,
                        label = "Alerts",
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAlerts
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Income vs Expenses
            item {
                IncomeExpenseCard(
                    income = summary.monthlyIncome,
                    expenses = summary.totalExpenses,
                    savings = summary.netSavings,
                    savingsRate = summary.savingsRate
                )
            }

            // Budget Utilization
            if (summary.budgetUtilization > 0) {
                item {
                    BudgetUtilizationCard(utilization = summary.budgetUtilization)
                }
            }

            // Overspending Alert
            if (summary.overspendingCategories.isNotEmpty()) {
                item {
                    OverspendingAlertCard(categories = summary.overspendingCategories)
                }
            }

            // Savings Goals Summary
            item {
                GoalsSummaryCard(
                    progress = summary.totalGoalProgress,
                    activeCount = summary.activeGoalCount,
                    atRiskCount = summary.atRiskGoalCount
                )
            }

            // Top Expense
            if (summary.topExpenseCategory != "None") {
                item {
                    TopExpenseCard(
                        category = summary.topExpenseCategory,
                        amount = summary.topExpenseAmount
                    )
                }
            }

            // Category Breakdown
            if (categoryTotals.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "This Month's Spending",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val chartData = categoryTotals.associate { it.category to it.total }
                            PieChart(data = chartData)
                        }
                    }
                }
            }

            // Quick Tips
            item {
                QuickTipsCard(savingsRate = summary.savingsRate)
            }
        }
    }
}

@Composable
private fun IncomeExpenseCard(
    income: Double,
    expenses: Double,
    savings: Double,
    savingsRate: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Monthly Overview", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Text("Income", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        CurrencyUtils.format(income),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                    Text("Expenses", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        CurrencyUtils.format(expenses),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Savings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text("Savings", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        CurrencyUtils.format(savings),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (savings >= 0) MaterialTheme.colorScheme.primary else Color(0xFFF44336)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Savings Rate", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(
                    "${String.format("%.1f", savingsRate)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (savingsRate >= 20) Color(0xFF4CAF50) else if (savingsRate >= 10) Color(0xFFFF9800) else Color(0xFFF44336)
                )
            }

            LinearProgressIndicator(
                progress = (savingsRate / 100).toFloat().coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    savingsRate >= 20 -> Color(0xFF4CAF50)
                    savingsRate >= 10 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                },
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun BudgetUtilizationCard(utilization: Float) {
    val color = when {
        utilization > 1.0f -> Color(0xFFF44336)
        utilization > 0.8f -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Budget Used", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "${String.format("%.0f", utilization * 100)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = utilization.coerceIn(0f, 1.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )

            if (utilization > 1.0f) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "You've exceeded your budget by ${String.format("%.0f", (utilization - 1) * 100)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun OverspendingAlertCard(categories: List<OverspendingCategory>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Over Budget Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            categories.take(3).forEach { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            category.category,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Budget: ${CurrencyUtils.format(category.budget)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            CurrencyUtils.format(category.actual),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                        Text(
                            "+${String.format("%.0f", category.percentOver)}% over",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF44336)
                        )
                    }
                }
                if (category != categories.last()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            if (categories.size > 3) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "+${categories.size - 3} more categories over budget",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun GoalsSummaryCard(progress: Float, activeCount: Int, atRiskCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Savings Goals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Text("$activeCount active", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${String.format("%.1f", progress * 100)}% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (atRiskCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "$atRiskCount at risk",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopExpenseCard(category: String, amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        category.take(2),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("Top Expense Category", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(category, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(
                CurrencyUtils.format(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun QuickAccessButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun QuickTipsCard(savingsRate: Double) {
    val tip = when {
        savingsRate >= 30 -> "Excellent! You're saving over 30% of your income. Consider investing the surplus."
        savingsRate >= 20 -> "Great job! You're meeting the recommended 20% savings rate."
        savingsRate >= 10 -> "Good start! Try to increase savings to 20% by reducing wants spending."
        savingsRate > 0 -> "You're saving, but consider cutting back on non-essential expenses."
        else -> "You're spending more than you earn. Review your budget and cut unnecessary costs."
    }

    val tipColor = when {
        savingsRate >= 20 -> Color(0xFFE8F5E9)
        savingsRate >= 10 -> Color(0xFFFFF3E0)
        else -> Color(0xFFFFEBEE)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = tipColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Smart Tip",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(tip, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
