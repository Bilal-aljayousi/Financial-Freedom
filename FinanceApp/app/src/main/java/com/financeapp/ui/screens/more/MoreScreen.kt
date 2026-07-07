package com.financeapp.ui.screens.more

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToAlerts: () -> Unit,
    onNavigateToSalary: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More") },
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
            item {
                Text(
                    "Tools & Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Spending Alerts",
                    subtitle = "Budget warnings and overspending notifications",
                    iconColor = Color(0xFFFF9800),
                    onClick = onNavigateToAlerts
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Savings,
                    title = "Salary Planner",
                    subtitle = "Set salary and budget allocation percentages",
                    iconColor = Color(0xFF4CAF50),
                    onClick = onNavigateToSalary
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.AccountBalance,
                    title = "Investment Portfolio",
                    subtitle = "Track stocks and investment holdings",
                    iconColor = Color(0xFF2196F3),
                    onClick = onNavigateToPortfolio
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Calculate,
                    title = "Investment Calculator",
                    subtitle = "Calculate compound interest and returns",
                    iconColor = Color(0xFF9C27B0),
                    onClick = onNavigateToCalculator
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Backup,
                    title = "Backup & Restore",
                    subtitle = "Export and import your financial data",
                    iconColor = Color(0xFF607D8B),
                    onClick = onNavigateToBackup
                )
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
