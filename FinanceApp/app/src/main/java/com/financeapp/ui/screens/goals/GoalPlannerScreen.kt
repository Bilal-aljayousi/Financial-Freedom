package com.financeapp.ui.screens.goals

import android.app.DatePickerDialog
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.financeapp.data.GoalMilestone
import com.financeapp.data.SavingsGoal
import com.financeapp.util.CurrencyUtils
import com.financeapp.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalPlannerScreen(viewModel: GoalPlannerViewModel) {
    val context = LocalContext.current
    val goalsWithMilestones by viewModel.goalWithMilestones.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val showAddGoalDialog by viewModel.showAddGoalDialog.collectAsState()
    val showAddMilestoneDialog by viewModel.showAddMilestoneDialog.collectAsState()
    val goalFormData by viewModel.goalFormData.collectAsState()
    val milestoneFormData by viewModel.milestoneFormData.collectAsState()
    val message by viewModel.message.collectAsState()
    val selectedGoalId by viewModel.selectedGoalId.collectAsState()
    val selectedGoalMilestones by viewModel.selectedGoalMilestones.collectAsState()

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    val selectedGoal = if (selectedGoalId != null) {
        goalsWithMilestones.find { it.goal.id == selectedGoalId }
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (selectedGoal != null) {
                        Text(selectedGoal.goal.name)
                    } else {
                        Text("Goal Planner")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    if (selectedGoal != null) {
                        IconButton(onClick = { viewModel.selectGoal(null) }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedGoal != null) {
                        viewModel.showAddMilestoneDialog(selectedGoal.goal.id, true)
                    } else {
                        viewModel.showAddGoalDialog(true)
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        if (selectedGoal != null) {
            GoalDetailView(
                goalWithMilestones = selectedGoal,
                milestones = selectedGoalMilestones,
                onAddMilestone = { viewModel.showAddMilestoneDialog(selectedGoal.goal.id, true) },
                onDeleteMilestone = { viewModel.deleteMilestone(it) },
                onUpdateMilestoneAmount = { id, amount -> viewModel.setMilestoneAmount(id, amount) },
                onDeleteGoal = { viewModel.deleteGoal(selectedGoal.goal) },
                modifier = Modifier.padding(padding)
            )
        } else {
            GoalPlannerListView(
                goalsWithMilestones = goalsWithMilestones,
                summary = summary,
                onSelectGoal = { viewModel.selectGoal(it.goal.id) },
                onDeleteGoal = { viewModel.deleteGoal(it.goal) },
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            formData = goalFormData,
            onFormChange = { viewModel.updateGoalForm(it) },
            onConfirm = { viewModel.createGoal() },
            onDismiss = { viewModel.showAddGoalDialog(false) }
        )
    }

    if (showAddMilestoneDialog) {
        AddMilestoneDialog(
            formData = milestoneFormData,
            onFormChange = { viewModel.updateMilestoneForm(it) },
            onConfirm = { viewModel.createMilestone() },
            onDismiss = { viewModel.showAddMilestoneDialog(milestoneFormData.goalId, false) }
        )
    }
}

@Composable
private fun GoalPlannerListView(
    goalsWithMilestones: List<GoalWithMilestones>,
    summary: GoalPlannerSummary,
    onSelectGoal: (GoalWithMilestones) -> Unit,
    onDeleteGoal: (GoalWithMilestones) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Card
        item {
            GoalPlannerSummaryCard(summary = summary)
        }

        // Salary Allocation
        summary.salaryConfig?.let { config ->
            if (config.monthlySalary > 0) {
                item {
                    SalaryAllocationCard(summary = summary)
                }
            }
        }

        // Goals List
        if (goalsWithMilestones.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No goals yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Text(
                            "Tap + to create your first goal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(goalsWithMilestones) { goalWithMilestones ->
                GoalCard(
                    goalWithMilestones = goalWithMilestones,
                    onClick = { onSelectGoal(goalWithMilestones) },
                    onDelete = { onDeleteGoal(goalWithMilestones) }
                )
            }
        }
    }
}

@Composable
private fun GoalDetailView(
    goalWithMilestones: GoalWithMilestones,
    milestones: List<GoalMilestone>,
    onAddMilestone: () -> Unit,
    onDeleteMilestone: (GoalMilestone) -> Unit,
    onUpdateMilestoneAmount: (Long, Double) -> Unit,
    onDeleteGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goal = goalWithMilestones.goal
    var showDeleteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Goal Overview
        item {
            GoalOverviewCard(goalWithMilestones = goalWithMilestones)
        }

        // Milestones Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Milestones (${goalWithMilestones.completedMilestoneCount}/${goalWithMilestones.totalMilestoneCount})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (milestones.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No milestones yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onAddMilestone) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add First Milestone")
                        }
                    }
                }
            }
        } else {
            items(milestones) { milestone ->
                MilestoneCard(
                    milestone = milestone,
                    onUpdateAmount = { amount -> onUpdateMilestoneAmount(milestone.id, amount) },
                    onDelete = { onDeleteMilestone(milestone) }
                )
            }
        }

        // Salary Integration
        item {
            Spacer(modifier = Modifier.height(8.dp))
            MilestoneSalaryIntegrationCard(
                goal = goal,
                milestones = milestones,
                monthlyAllocation = goalWithMilestones.monthlyAllocation
            )
        }
    }
}

@Composable
private fun GoalPlannerSummaryCard(summary: GoalPlannerSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Goal Planner Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${summary.activeGoals}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Active", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${summary.completedGoals}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text("Completed", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${summary.totalGoals}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Total", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Overall Progress", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(
                    "${String.format("%.1f", summary.overallProgress * 100)}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            LinearProgressIndicator(
                progress = summary.overallProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Saved: ${CurrencyUtils.format(summary.totalCurrentAmount)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Target: ${CurrencyUtils.format(summary.totalTargetAmount)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SalaryAllocationCard(summary: GoalPlannerSummary) {
    val salary = summary.salaryConfig?.monthlySalary ?: 0.0
    val savingsPercent = summary.salaryConfig?.savingsPercent ?: 20.0
    val monthlySavings = salary * (savingsPercent / 100.0)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Monthly Salary Allocation",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Monthly Salary", style = MaterialTheme.typography.bodySmall)
                Text(CurrencyUtils.format(salary), fontWeight = FontWeight.Medium)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Savings (${String.format("%.0f", savingsPercent)}%)", style = MaterialTheme.typography.bodySmall)
                Text(CurrencyUtils.format(monthlySavings), fontWeight = FontWeight.Medium)
            }

            if (summary.monthlySavingsNeeded > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val isEnough = monthlySavings >= summary.monthlySavingsNeeded
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Needed for goals",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isEnough) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Text(
                        CurrencyUtils.format(summary.monthlySavingsNeeded),
                        fontWeight = FontWeight.Medium,
                        color = if (isEnough) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }

                if (!isEnough) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "You need ${CurrencyUtils.format(summary.monthlySavingsNeeded - monthlySavings)} more per month",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalCard(
    goalWithMilestones: GoalWithMilestones,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val goal = goalWithMilestones.goal
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    val isAtRisk = goalWithMilestones.isAtRisk

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Flag,
                        contentDescription = null,
                        tint = if (isAtRisk) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row {
                    if (isAtRisk) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "At Risk",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    CurrencyUtils.format(goal.currentAmount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    CurrencyUtils.format(goal.targetAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    progress >= 1.0f -> Color(0xFF4CAF50)
                    isAtRisk -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${String.format("%.1f", progress * 100)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    "Deadline: ${DateUtils.formatDate(goal.deadline)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (goalWithMilestones.totalMilestoneCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Milestones: ${goalWithMilestones.completedMilestoneCount}/${goalWithMilestones.totalMilestoneCount} completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun GoalOverviewCard(goalWithMilestones: GoalWithMilestones) {
    val goal = goalWithMilestones.goal
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    goal.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (goalWithMilestones.isAtRisk) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("At Risk", color = Color(0xFFFF9800), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        CurrencyUtils.format(goal.currentAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Saved", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        CurrencyUtils.format(goal.targetAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Target", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        CurrencyUtils.format(goalWithMilestones.monthlyAllocation),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text("Monthly", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (progress >= 1.0f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
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
                Text(
                    "Deadline: ${DateUtils.formatDate(goal.deadline)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (goalWithMilestones.totalMilestoneCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Milestones: ${goalWithMilestones.completedMilestoneCount}/${goalWithMilestones.totalMilestoneCount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "${CurrencyUtils.format(goalWithMilestones.milestoneCurrentSum)} / ${CurrencyUtils.format(goalWithMilestones.milestoneTargetSum)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneCard(
    milestone: GoalMilestone,
    onUpdateAmount: (Double) -> Unit,
    onDelete: () -> Unit
) {
    var showAmountDialog by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }

    val progress = milestone.progress
    val isCompleted = milestone.isCompleted
    val isOverdue = milestone.isOverdue

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (isOverdue) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Overdue",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            milestone.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        if (milestone.description.isNotBlank()) {
                            Text(
                                milestone.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = { showAmountDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Update Amount",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    CurrencyUtils.format(milestone.currentAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    CurrencyUtils.format(milestone.targetAmount),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    isCompleted -> Color(0xFF4CAF50)
                    isOverdue -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${String.format("%.1f", progress * 100)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    "Due: ${DateUtils.formatDate(milestone.deadline)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) Color(0xFFF44336) else Color.Gray
                )
            }
        }
    }

    if (showAmountDialog) {
        AlertDialog(
            onDismissRequest = { showAmountDialog = false },
            title = { Text("Update Amount") },
            text = {
                Column {
                    Text("Current: ${CurrencyUtils.format(milestone.currentAmount)}")
                    Text("Target: ${CurrencyUtils.format(milestone.targetAmount)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("New Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        amountText.toDoubleOrNull()?.let { onUpdateAmount(it) }
                        showAmountDialog = false
                        amountText = ""
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAmountDialog = false; amountText = "" }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MilestoneSalaryIntegrationCard(
    goal: SavingsGoal,
    milestones: List<GoalMilestone>,
    monthlyAllocation: Double
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Salary Integration",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
            val daysLeft = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(
                goal.deadline - System.currentTimeMillis()
            )
            val monthsLeft = (daysLeft / 30.0).coerceAtLeast(1.0)
            val monthlyNeeded = if (remaining > 0) remaining / monthsLeft else 0.0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Monthly Allocation", style = MaterialTheme.typography.bodySmall)
                Text(
                    CurrencyUtils.format(monthlyAllocation),
                    fontWeight = FontWeight.Medium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Monthly Needed", style = MaterialTheme.typography.bodySmall)
                Text(
                    CurrencyUtils.format(monthlyNeeded),
                    fontWeight = FontWeight.Medium,
                    color = if (monthlyAllocation >= monthlyNeeded) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }

            if (monthlyNeeded > monthlyAllocation) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Shortfall: ${CurrencyUtils.format(monthlyNeeded - monthlyAllocation)}/month",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336)
                )
            }

            if (milestones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Per Milestone (equal split):",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                val perMilestone = if (milestones.isNotEmpty()) monthlyAllocation / milestones.size else 0.0
                milestones.filter { !it.isCompleted }.take(3).forEach { milestone ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            milestone.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            CurrencyUtils.format(perMilestone),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    formData: GoalFormData,
    onFormChange: (GoalFormData) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = formData.name,
                    onValueChange = { onFormChange(formData.copy(name = it)) },
                    label = { Text("Goal Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = formData.targetAmount,
                    onValueChange = { onFormChange(formData.copy(targetAmount = it)) },
                    label = { Text("Target Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        calendar.timeInMillis = formData.deadline
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(year, month, day)
                                onFormChange(formData.copy(deadline = calendar.timeInMillis))
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Deadline: ${DateUtils.formatDate(formData.deadline)}")
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMilestoneDialog(
    formData: MilestoneFormData,
    onFormChange: (MilestoneFormData) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Milestone") },
        text = {
            Column {
                OutlinedTextField(
                    value = formData.title,
                    onValueChange = { onFormChange(formData.copy(title = it)) },
                    label = { Text("Milestone Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = formData.description,
                    onValueChange = { onFormChange(formData.copy(description = it)) },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = formData.targetAmount,
                    onValueChange = { onFormChange(formData.copy(targetAmount = it)) },
                    label = { Text("Target Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        calendar.timeInMillis = formData.deadline
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(year, month, day)
                                onFormChange(formData.copy(deadline = calendar.timeInMillis))
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Deadline: ${DateUtils.formatDate(formData.deadline)}")
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
