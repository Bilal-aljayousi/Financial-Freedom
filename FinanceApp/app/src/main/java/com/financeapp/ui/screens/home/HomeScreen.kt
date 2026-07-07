package com.financeapp.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.financeapp.util.CurrencyUtils
import com.financeapp.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val summary by viewModel.summary.collectAsState()
    var showAddSavingDialog by remember { mutableStateOf(false) }

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
        if (summary.activeGoal == null) {
            // No active goal
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Today's Saving Card
                item {
                    TodaySavingCard(
                        goalName = summary.activeGoal!!.name,
                        dailyNeeded = summary.dailyNeeded,
                        currency = summary.currency,
                        onAddSaving = { showAddSavingDialog = true }
                    )
                }

                // Goal Progress
                item {
                    GoalProgressCard(
                        goalName = summary.activeGoal!!.name,
                        currentAmount = summary.activeGoal!!.currentAmount,
                        targetAmount = summary.activeGoal!!.targetAmount,
                        progress = summary.activeGoal!!.progress,
                        daysRemaining = summary.daysRemaining,
                        monthsRemaining = summary.monthsRemaining,
                        currency = summary.currency
                    )
                }

                // Monthly Target
                item {
                    MonthlyTargetCard(
                        monthlyNeeded = summary.monthlyNeeded,
                        salary = summary.salary,
                        currency = summary.currency
                    )
                }

                // Status
                item {
                    StatusCard(onTrack = summary.onTrack)
                }

                // All Goals Summary
                if (summary.totalTarget > 0) {
                    item {
                        AllGoalsSummaryCard(
                            totalSaved = summary.totalSaved,
                            totalTarget = summary.totalTarget,
                            progress = summary.overallProgress,
                            currency = summary.currency
                        )
                    }
                }
            }
        }
    }

    if (showAddSavingDialog && summary.activeGoal != null) {
        AddSavingDialog(
            goalName = summary.activeGoal!!.name,
            dailyNeeded = summary.dailyNeeded,
            currency = summary.currency,
            onAdd = { amount ->
                viewModel.addSavingToGoal(summary.activeGoal!!.id, amount)
                showAddSavingDialog = false
            },
            onDismiss = { showAddSavingDialog = false }
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Savings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No savings goal yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Go to Goals tab to create your first savings goal",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TodaySavingCard(
    goalName: String,
    dailyNeeded: Double,
    currency: String,
    onAddSaving: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Today's Saving",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                CurrencyUtils.format(dailyNeeded, currency),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "for $goalName",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Savings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Saving")
            }
        }
    }
}

@Composable
private fun GoalProgressCard(
    goalName: String,
    currentAmount: Double,
    targetAmount: Double,
    progress: Float,
    daysRemaining: Long,
    monthsRemaining: Int,
    currency: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                goalName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = when {
                    progress >= 1.0f -> Color(0xFF4CAF50)
                    progress >= 0.5f -> MaterialTheme.colorScheme.primary
                    else -> Color(0xFFFF9800)
                },
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Saved", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        CurrencyUtils.format(currentAmount, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Progress", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        "${String.format("%.1f", progress * 100)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Target", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        CurrencyUtils.format(targetAmount, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "$monthsRemaining months left",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    "$daysRemaining days left",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun MonthlyTargetCard(
    monthlyNeeded: Double,
    salary: Double,
    currency: String
) {
    val percentOfSalary = if (salary > 0) (monthlyNeeded / salary * 100) else 0.0

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Monthly Target",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Need to save", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${CurrencyUtils.format(monthlyNeeded, currency)}/month",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (salary > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Of your salary", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        "${String.format("%.1f", percentOfSalary)}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (percentOfSalary <= 30) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(onTrack: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (onTrack) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (onTrack) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (onTrack) Color(0xFF4CAF50) else Color(0xFFFF9800),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    if (onTrack) "On Track!" else "Behind Schedule",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (onTrack) Color(0xFF2E7D32) else Color(0xFFE65100)
                )
                Text(
                    if (onTrack) "You're doing great! Keep saving daily."
                    else "Try to save a bit more each day to catch up.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (onTrack) Color(0xFF2E7D32) else Color(0xFFBF360C)
                )
            }
        }
    }
}

@Composable
private fun AllGoalsSummaryCard(
    totalSaved: Double,
    totalTarget: Double,
    progress: Float,
    currency: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "All Goals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Saved: ${CurrencyUtils.format(totalSaved, currency)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Target: ${CurrencyUtils.format(totalTarget, currency)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun AddSavingDialog(
    goalName: String,
    dailyNeeded: Double,
    currency: String,
    onAdd: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Saving") },
        text = {
            Column {
                Text("Add to: $goalName")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Suggested: ${CurrencyUtils.format(dailyNeeded, currency)} (today's target)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount ($currency)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = amount.toDoubleOrNull()
                    if (value != null && value > 0) {
                        onAdd(value)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
