package com.financeapp.ui.screens.salary

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.financeapp.ui.theme.ChartColors
import com.financeapp.util.BudgetGroup
import com.financeapp.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryPlannerScreen(viewModel: SalaryPlannerViewModel) {
    val salaryConfig by viewModel.salaryConfig.collectAsState()
    val budgetGroups by viewModel.budgetGroups.collectAsState()
    val allocations by viewModel.allocations.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val editingSalary by viewModel.editingSalary.collectAsState()
    val editingNeeds by viewModel.editingNeedsPercent.collectAsState()
    val editingWants by viewModel.editingWantsPercent.collectAsState()
    val editingSavings by viewModel.editingSavingsPercent.collectAsState()
    val showEditAllocation by viewModel.showEditAllocation.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Salary Planner") },
                actions = {
                    IconButton(onClick = { viewModel.startEditing() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                },
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
            if (isEditing) {
                item {
                    EditSalaryConfigCard(
                        salary = editingSalary,
                        needs = editingNeeds,
                        wants = editingWants,
                        savings = editingSavings,
                        onSalaryChange = { viewModel.updateSalary(it) },
                        onNeedsChange = { viewModel.updateNeedsPercent(it) },
                        onWantsChange = { viewModel.updateWantsPercent(it) },
                        onSavingsChange = { viewModel.updateSavingsPercent(it) },
                        onSave = { viewModel.saveConfig() },
                        onCancel = { viewModel.cancelEditing() }
                    )
                }
            } else {
                // Salary Overview
                item {
                    SalaryOverviewCard(
                        salary = salaryConfig?.monthlySalary ?: 0.0,
                        needsPercent = salaryConfig?.needsPercent ?: 50.0,
                        wantsPercent = salaryConfig?.wantsPercent ?: 30.0,
                        savingsPercent = salaryConfig?.savingsPercent ?: 20.0
                    )
                }

                // Budget Groups
                items(budgetGroups) { group ->
                    BudgetGroupCard(group = group)
                }

                // Individual Allocations
                item {
                    Text(
                        "Category Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(allocations) { allocation ->
                    AllocationItem(
                        allocation = allocation,
                        salary = salaryConfig?.monthlySalary ?: 0.0,
                        onEdit = { viewModel.showEditAllocation(allocation) }
                    )
                }
            }
        }
    }

    showEditAllocation?.let { allocation ->
        EditAllocationDialog(
            allocation = allocation,
            salary = salaryConfig?.monthlySalary ?: 0.0,
            onDismiss = { viewModel.dismissEditAllocation() },
            onSave = { newPercent -> viewModel.saveAllocation(allocation, newPercent) }
        )
    }
}

@Composable
private fun SalaryOverviewCard(
    salary: Double,
    needsPercent: Double,
    wantsPercent: Double,
    savingsPercent: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Monthly Salary", style = MaterialTheme.typography.titleMedium)
            Text(
                text = CurrencyUtils.format(salary),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Allocation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                LinearProgressIndicator(
                    progress = (needsPercent / 100).toFloat(),
                    modifier = Modifier.weight(needsPercent.toFloat()),
                    color = ChartColors[0]
                )
                LinearProgressIndicator(
                    progress = (wantsPercent / 100).toFloat(),
                    modifier = Modifier.weight(wantsPercent.toFloat()),
                    color = ChartColors[1]
                )
                LinearProgressIndicator(
                    progress = (savingsPercent / 100).toFloat(),
                    modifier = Modifier.weight(savingsPercent.toFloat()),
                    color = ChartColors[4]
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(ChartColors[0], "Needs ${String.format("%.0f", needsPercent)}%")
                LegendItem(ChartColors[1], "Wants ${String.format("%.0f", wantsPercent)}%")
                LegendItem(ChartColors[4], "Savings ${String.format("%.0f", savingsPercent)}%")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .padding(end = 4.dp)
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun BudgetGroupCard(group: BudgetGroup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${String.format("%.0f", group.percent)}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = CurrencyUtils.format(group.amount),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            group.allocations.forEach { alloc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(alloc.category, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${CurrencyUtils.format(alloc.amount)} (${String.format("%.0f", alloc.percent)}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun AllocationItem(
    allocation: com.financeapp.data.BudgetAllocation,
    salary: Double,
    onEdit: () -> Unit
) {
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
            Column {
                Text(
                    text = allocation.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${String.format("%.0f", allocation.allocatedPercent)}% of salary",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CurrencyUtils.format(salary * (allocation.allocatedPercent / 100)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
        }
    }
}

@Composable
private fun EditSalaryConfigCard(
    salary: String,
    needs: String,
    wants: String,
    savings: String,
    onSalaryChange: (String) -> Unit,
    onNeedsChange: (String) -> Unit,
    onWantsChange: (String) -> Unit,
    onSavingsChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Edit Salary & Allocation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = salary,
                onValueChange = onSalaryChange,
                label = { Text("Monthly Salary") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("$") }
            )

            OutlinedTextField(
                value = needs,
                onValueChange = onNeedsChange,
                label = { Text("Needs %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text("%") }
            )

            OutlinedTextField(
                value = wants,
                onValueChange = onWantsChange,
                label = { Text("Wants %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text("%") }
            )

            OutlinedTextField(
                value = savings,
                onValueChange = onSavingsChange,
                label = { Text("Savings %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text("%") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onSave, modifier = Modifier.weight(1f)) {
                    Text("Save")
                }
                TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun EditAllocationDialog(
    allocation: com.financeapp.data.BudgetAllocation,
    salary: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var percent by remember { mutableStateOf(allocation.allocatedPercent.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${allocation.category}") },
        text = {
            Column {
                Text("Current: ${CurrencyUtils.format(salary * (allocation.allocatedPercent / 100))} (${allocation.allocatedPercent}%)")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = percent,
                    onValueChange = { percent = it },
                    label = { Text("New Percentage") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text("%") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                percent.toDoubleOrNull()?.let { onSave(it) }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
