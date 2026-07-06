package com.financeapp.ui.screens.reports

import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financeapp.ui.components.PieChart
import com.financeapp.util.CurrencyUtils

private val chartColors = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFF44336),
    Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportsViewModel) {
    val context = LocalContext.current
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val trends by viewModel.trends.collectAsState()
    val exportMessage by viewModel.exportMessage.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val salaryVsExpenses by viewModel.salaryVsExpenses.collectAsState()
    val savingsGrowth by viewModel.savingsGrowth.collectAsState()
    val weeklySpending by viewModel.weeklySpending.collectAsState()
    val milestoneProgress by viewModel.milestoneProgress.collectAsState()

    LaunchedEffect(exportMessage) {
        exportMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Insights") },
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
            // Period Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPeriod == "weekly",
                        onClick = { viewModel.setPeriod("weekly") },
                        label = { Text("Weekly") }
                    )
                    FilterChip(
                        selected = selectedPeriod == "monthly",
                        onClick = { viewModel.setPeriod("monthly") },
                        label = { Text("Monthly") }
                    )
                    FilterChip(
                        selected = selectedPeriod == "yearly",
                        onClick = { viewModel.setPeriod("yearly") },
                        label = { Text("Yearly") }
                    )
                }
            }

            // Category Pie Chart
            if (categoryTotals.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Spending by Category",
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

            // Salary vs Expenses Section
            salaryVsExpenses?.let { data ->
                if (data.salary > 0) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Salary vs Expenses",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Salary: ${CurrencyUtils.format(data.salary)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "Spent: ${CurrencyUtils.format(data.totalExpenses)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (data.totalExpenses > data.salary) Color(0xFFF44336) else Color(0xFF4CAF50)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val maxVal = maxOf(data.salary, data.totalExpenses, 1.0)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE0E0E0))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((data.totalExpenses / maxVal).toFloat())
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (data.totalExpenses > data.salary) Color(0xFFF44336)
                                                else Color(0xFF4CAF50)
                                            )
                                    )
                                }

                                val remaining = data.salary - data.totalExpenses
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    if (remaining >= 0) "Remaining: ${CurrencyUtils.format(remaining)}"
                                    else "Over budget: ${CurrencyUtils.format(-remaining)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (remaining >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    fontWeight = FontWeight.Medium
                                )

                                if (data.overspentCategories.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Over-Budget Categories",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFF44336)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    data.overspentCategories.forEach { cat ->
                                        val comp = data.categoryBreakdown.find { it.category == cat }
                                        comp?.let {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(cat, style = MaterialTheme.typography.bodySmall)
                                                Text(
                                                    "${CurrencyUtils.format(it.actual)} / ${CurrencyUtils.format(it.budget)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFFF44336)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Weekly Spending Chart
            if (weeklySpending.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "This Week's Spending",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val maxAmount = weeklySpending.maxOfOrNull { it.amount } ?: 1.0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                weeklySpending.forEach { day ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (day.amount > 0) {
                                            Text(
                                                CurrencyUtils.format(day.amount),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .height((if (maxAmount > 0) (day.amount / maxAmount * 80).dp else 0.dp))
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Text(
                                            day.dayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Savings Growth Line Chart
            if (savingsGrowth.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Savings Growth (6 Months)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Target", style = MaterialTheme.typography.labelSmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2196F3))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Actual Cumulative", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            ) {
                                val targetLine = Color(0xFF4CAF50)
                                val actualLine = Color(0xFF2196F3)
                                val gridColor = Color(0xFFE0E0E0)

                                val maxVal = savingsGrowth.maxOfOrNull { maxOf(it.targetSavings, it.cumulativeSavings) } ?: 1.0
                                val stepX = size.width / (savingsGrowth.size - 1).coerceAtLeast(1)

                                for (i in 0..4) {
                                    val y = size.height * i / 4
                                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                                }

                                val targetPath = Path()
                                savingsGrowth.forEachIndexed { index, point ->
                                    val x = index * stepX
                                    val y = size.height - (point.targetSavings / maxVal * size.height).toFloat()
                                    if (index == 0) targetPath.moveTo(x, y) else targetPath.lineTo(x, y)
                                }
                                drawPath(targetPath, targetLine, style = Stroke(width = 3f))

                                val actualPath = Path()
                                savingsGrowth.forEachIndexed { index, point ->
                                    val x = index * stepX
                                    val y = size.height - (point.cumulativeSavings / maxVal * size.height).toFloat()
                                    if (index == 0) actualPath.moveTo(x, y) else actualPath.lineTo(x, y)
                                }
                                drawPath(actualPath, actualLine, style = Stroke(width = 3f))

                                savingsGrowth.forEachIndexed { index, point ->
                                    val x = index * stepX

                                    val targetY = size.height - (point.targetSavings / maxVal * size.height).toFloat()
                                    drawCircle(targetLine, 5f, Offset(x, targetY))

                                    val actualY = size.height - (point.cumulativeSavings / maxVal * size.height).toFloat()
                                    drawCircle(actualLine, 5f, Offset(x, actualY))

                                    drawContext.canvas.nativeCanvas.drawText(
                                        point.month,
                                        x - 10f,
                                        size.height + 30f,
                                        android.graphics.Paint().apply {
                                            textSize = 24f
                                            color = android.graphics.Color.GRAY
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Milestone Progress Section
            if (milestoneProgress.isNotEmpty()) {
                item {
                    Text(
                        "Goal Milestone Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(milestoneProgress) { progress ->
                    MilestoneProgressCard(progress = progress)
                }

                // Milestone Bar Chart
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Milestones by Goal",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val maxTarget = milestoneProgress.maxOfOrNull { it.totalTarget } ?: 1.0

                            milestoneProgress.forEach { progress ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        progress.goalName,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.width(80.dp),
                                        maxLines = 1
                                    )
                                    LinearProgressIndicator(
                                        progress = progress.progress,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = if (progress.progress >= 1.0f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "${progress.completedMilestones}/${progress.totalMilestones}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        modifier = Modifier.width(40.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Spending Trends
            if (trends.isNotEmpty()) {
                item {
                    Text(
                        "Month-over-Month Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(trends) { trend ->
                    TrendItem(trend = trend)
                }
            }

            // Monthly Insights
            if (insights.isNotEmpty()) {
                item {
                    Text(
                        "Monthly Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(insights) { insight ->
                    InsightCard(insight = insight)
                }
            }

            // Export Section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Export Reports",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { viewModel.exportExpensesCsv() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Expenses (CSV)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.exportGoalsCsv() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Goals (CSV)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.exportFullReport() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Full Report (TXT)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.shareExport() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Last Export")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneProgressCard(progress: MilestoneProgressData) {
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
                    Icon(
                        Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        progress.goalName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    "${progress.completedMilestones}/${progress.totalMilestones}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (progress.progress >= 1.0f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${String.format("%.1f", progress.progress * 100)}% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    "${CurrencyUtils.format(progress.totalCurrent)} / ${CurrencyUtils.format(progress.totalTarget)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun TrendItem(trend: SpendingTrend) {
    val isIncrease = trend.change > 0
    val changeColor = if (isIncrease) Color(0xFFF44336) else Color(0xFF4CAF50)

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    trend.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Last: ${CurrencyUtils.format(trend.lastMonth)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyUtils.format(trend.thisMonth),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "${if (isIncrease) "+" else ""}${String.format("%.1f", trend.changePercent)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = changeColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightCard(insight: MonthlyInsight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    insight.month,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Saved: ${CurrencyUtils.format(insight.savings)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (insight.savings >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total spent: ${CurrencyUtils.format(insight.totalExpenses)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Top: ${insight.topCategory} (${CurrencyUtils.format(insight.topCategoryAmount)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
