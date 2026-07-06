package com.financeapp.ui.screens.alerts

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dismiss
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financeapp.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(viewModel: AlertsViewModel) {
    val alerts by viewModel.alerts.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val totalBudget by viewModel.totalBudget.collectAsState()
    val isOverBudget by viewModel.isOverBudget.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending Alerts") },
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
            // Budget Overview
            item {
                BudgetOverviewCard(
                    totalSpent = totalSpent,
                    totalBudget = totalBudget,
                    isOverBudget = isOverBudget
                )
            }

            // Alert Summary
            item {
                when {
                    alerts.isEmpty() -> AllClearCard()
                    alerts.any { it.severity == AlertSeverity.CRITICAL } -> {
                        CriticalAlertCard(criticalCount = alerts.count { it.severity == AlertSeverity.CRITICAL })
                    }
                    else -> {
                        WarningAlertCard(warningCount = alerts.count { it.severity == AlertSeverity.WARNING })
                    }
                }
            }

            // Individual Alerts
            if (alerts.isNotEmpty()) {
                item {
                    Text(
                        "Category Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(alerts) { alert ->
                    AlertItemCard(
                        alert = alert,
                        onDismiss = { viewModel.dismissAlert(alert.category) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetOverviewCard(totalSpent: Double, totalBudget: Double, isOverBudget: Boolean) {
    val progress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1.5f) else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverBudget) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Monthly Budget Overview", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Spent", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        CurrencyUtils.format(totalSpent),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverBudget) Color(0xFFF44336) else MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Budget", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        CurrencyUtils.format(totalBudget),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = progress.coerceAtMost(1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = when {
                    progress > 1.0f -> Color(0xFFF44336)
                    progress > 0.8f -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                if (isOverBudget) "Over budget by ${CurrencyUtils.format(totalSpent - totalBudget)}"
                else "${CurrencyUtils.format(totalBudget - totalSpent)} remaining",
                style = MaterialTheme.typography.bodySmall,
                color = if (isOverBudget) Color(0xFFF44336) else Color.Gray
            )
        }
    }
}

@Composable
private fun AllClearCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "All Clear!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    "You're within all your budget limits. Keep up the good work!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1B5E20)
                )
            }
        }
    }
}

@Composable
private fun CriticalAlertCard(criticalCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Budget Exceeded!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFC62828)
                )
                Text(
                    "$criticalCount ${if (criticalCount > 1) "categories have" else "category has"} exceeded their budget limit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB71C1C)
                )
            }
        }
    }
}

@Composable
private fun WarningAlertCard(warningCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Approaching Limits",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE65100)
                )
                Text(
                    "$warningCount ${if (warningCount > 1) "categories are" else "category is"} approaching their budget limit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFBF360C)
                )
            }
        }
    }
}

@Composable
private fun AlertItemCard(alert: AlertItem, onDismiss: () -> Unit) {
    val cardColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> Color(0xFFFFF0F0)
        AlertSeverity.WARNING -> Color(0xFFFFF8E1)
        AlertSeverity.NORMAL -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                when (alert.severity) {
                                    AlertSeverity.CRITICAL -> Color(0xFFF44336)
                                    AlertSeverity.WARNING -> Color(0xFFFF9800)
                                    AlertSeverity.NORMAL -> Color(0xFF4CAF50)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        alert.category,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Dismiss, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = alert.percentUsed.coerceAtMost(1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when (alert.severity) {
                    AlertSeverity.CRITICAL -> Color(0xFFF44336)
                    AlertSeverity.WARNING -> Color(0xFFFF9800)
                    AlertSeverity.NORMAL -> Color(0xFF4CAF50)
                },
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Spent: ${CurrencyUtils.format(alert.spent)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Limit: ${CurrencyUtils.format(alert.limit)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                "${String.format("%.0f", alert.percentUsed * 100)}% used",
                style = MaterialTheme.typography.labelSmall,
                color = when (alert.severity) {
                    AlertSeverity.CRITICAL -> Color(0xFFF44336)
                    AlertSeverity.WARNING -> Color(0xFFFF9800)
                    AlertSeverity.NORMAL -> Color(0xFF4CAF50)
                }
            )
        }
    }
}
