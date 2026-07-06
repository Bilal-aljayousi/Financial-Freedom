package com.financeapp.ui.screens.goals

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.financeapp.util.CurrencyUtils
import com.financeapp.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDashboardScreen(viewModel: GoalDashboardViewModel) {
    val goalsWithProgress by viewModel.goalsWithProgress.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
    val savingsPotential by viewModel.savingsPotential.collectAsState()

    val totalTarget = goalsWithProgress.sumOf { it.goal.targetAmount }
    val totalCurrent = goalsWithProgress.sumOf { it.goal.currentAmount }
    val overallProgress = if (totalTarget > 0) (totalCurrent / totalTarget).toFloat() else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goal Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.toggleAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Savings Potential Indicator
            item {
                SavingsPotentialCard(savingsPotential = savingsPotential)
            }

            // Overspending Alert
            if (savingsPotential.overspendAmount > 0) {
                item {
                    OverspendAlertCard(overspendAmount = savingsPotential.overspendAmount)
                }
            }

            // Suggested Savings
            if (savingsPotential.suggestedSavings > 0) {
                item {
                    SuggestedSavingsCard(
                        suggestedAmount = savingsPotential.suggestedSavings,
                        maxSavings = savingsPotential.maxSavings
                    )
                }
            }

            // Overall Progress
            item {
                OverallProgressCard(
                    totalTarget = totalTarget,
                    totalCurrent = totalCurrent,
                    overallProgress = overallProgress,
                    goalCount = goalsWithProgress.size
                )
            }

            // At Risk Warning
            val atRiskGoals = goalsWithProgress.filter { it.isAtRisk }
            if (atRiskGoals.isNotEmpty()) {
                item {
                    AtRiskWarningCard(atRiskCount = atRiskGoals.size)
                }
            }

            // Goals List
            items(goalsWithProgress) { goalWithProgress ->
                GoalProgressCard(
                    goalWithProgress = goalWithProgress,
                    onDelete = { viewModel.deleteGoal(goalWithProgress.goal) },
                    onUpdateAmount = { amount -> viewModel.updateGoalAmount(goalWithProgress.goal, amount) }
                )
            }

            if (goalsWithProgress.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No goals yet. Tap + to add one.", color = Color.Gray)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { viewModel.toggleAddDialog() },
            onAdd = { name, amount, deadline ->
                viewModel.addGoal(name, amount, deadline)
            }
        )
    }

    showUpdateDialog?.let { goal ->
        UpdateAmountDialog(
            goal = goal,
            onDismiss = { viewModel.dismissUpdateDialog() },
            onUpdate = { amount -> viewModel.updateGoalAmount(goal, amount) }
        )
    }
}

@Composable
private fun SavingsPotentialCard(savingsPotential: SavingsPotential) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Savings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Savings Potential",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Current Savings", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        CurrencyUtils.format(savingsPotential.actualSavings),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Maximum Target", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        CurrencyUtils.format(savingsPotential.maxSavings),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = savingsPotential.potentialPercent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = when {
                    savingsPotential.potentialPercent >= 1.0f -> Color(0xFF4CAF50)
                    savingsPotential.potentialPercent >= 0.5f -> MaterialTheme.colorScheme.primary
                    else -> Color(0xFFFF9800)
                },
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "${String.format("%.1f", savingsPotential.potentialPercent * 100)}% of target reached",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun OverspendAlertCard(overspendAmount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.TrendingDown,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Overspending Detected",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFC62828)
                )
                Text(
                    "You've exceeded your salary by ${CurrencyUtils.format(overspendAmount)}. Consider reducing expenses to reach your goals faster.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB71C1C)
                )
            }
        }
    }
}

@Composable
private fun SuggestedSavingsCard(suggestedAmount: Double, maxSavings: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.TrendingUp,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Save More to Reach Goals",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    "Increase monthly savings by ${CurrencyUtils.format(suggestedAmount)} to hit your target of ${CurrencyUtils.format(maxSavings)}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1B5E20)
                )
            }
        }
    }
}

@Composable
private fun OverallProgressCard(
    totalTarget: Double,
    totalCurrent: Double,
    overallProgress: Float,
    goalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Overall Progress", style = MaterialTheme.typography.titleMedium)
            Text(
                text = CurrencyUtils.format(totalCurrent),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "of ${CurrencyUtils.format(totalTarget)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = overallProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${String.format("%.1f", overallProgress * 100)}% across $goalCount goals",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun AtRiskWarningCard(atRiskCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    "Goals at Risk",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE65100)
                )
                Text(
                    "$atRiskCount goal${if (atRiskCount > 1) "s" else ""} may miss their deadline. Consider increasing contributions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFBF360C)
                )
            }
        }
    }
}

@Composable
private fun GoalProgressCard(
    goalWithProgress: GoalWithProgress,
    onDelete: () -> Unit,
    onUpdateAmount: (Double) -> Unit
) {
    val goal = goalWithProgress.goal
    val progressColor = when {
        goalWithProgress.isAtRisk -> Color(0xFFFF9800)
        goalWithProgress.progress >= 1.0f -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.primary
    }

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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(progressColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(goalWithProgress.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = progressColor
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (goalWithProgress.isAtRisk) {
                            Text(
                                text = "AT RISK",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF9800),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = goalWithProgress.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${CurrencyUtils.format(goal.currentAmount)} / ${CurrencyUtils.format(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (goalWithProgress.daysRemaining > 0)
                        "${goalWithProgress.daysRemaining} days left"
                    else "OVERDUE",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (goalWithProgress.daysRemaining > 0) Color.Gray else Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Monthly needed: ${CurrencyUtils.format(goalWithProgress.monthlyNeeded)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Est. completion: ${goalWithProgress.estimatedMonths} months",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { onUpdateAmount(50.0) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+$50")
                }
                TextButton(
                    onClick = { onUpdateAmount(100.0) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+$100")
                }
                TextButton(
                    onClick = { onUpdateAmount(500.0) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+$500")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Savings Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    label = { Text("Target Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Text("Deadline: ${DateUtils.formatDate(deadline)}")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = targetAmount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amount > 0) {
                        onAdd(name, amount, deadline)
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = deadline)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { deadline = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun UpdateAmountDialog(
    goal: com.financeapp.data.SavingsGoal,
    onDismiss: () -> Unit,
    onUpdate: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to ${goal.name}") },
        text = {
            Column {
                Text("Current: ${CurrencyUtils.format(goal.currentAmount)}")
                Text("Target: ${CurrencyUtils.format(goal.targetAmount)}")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount to add") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = amount.toDoubleOrNull() ?: 0.0
                if (value > 0) onUpdate(value)
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
