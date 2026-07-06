package com.financeapp.ui.screens.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.BudgetAllocation
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import com.financeapp.data.SalaryConfig
import com.financeapp.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class AlertItem(
    val category: String,
    val spent: Double,
    val limit: Double,
    val percentUsed: Float,
    val severity: AlertSeverity
)

enum class AlertSeverity {
    NORMAL,
    WARNING,
    CRITICAL
}

class AlertsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository
    private val appContext = application

    val salaryConfig: StateFlow<SalaryConfig?>
    val allocations: StateFlow<List<BudgetAllocation>>

    private val _alerts = MutableStateFlow<List<AlertItem>>(emptyList())
    val alerts: StateFlow<List<AlertItem>> = _alerts.asStateFlow()

    private val _totalSpent = MutableStateFlow(0.0)
    val totalSpent: StateFlow<Double> = _totalSpent.asStateFlow()

    private val _totalBudget = MutableStateFlow(0.0)
    val totalBudget: StateFlow<Double> = _totalBudget.asStateFlow()

    private val _isOverBudget = MutableStateFlow(false)
    val isOverBudget: StateFlow<Boolean> = _isOverBudget.asStateFlow()

    private var lastCheckedMinute = -1

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        salaryConfig = repository.getSalaryConfig()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        allocations = repository.getAllAllocations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            combine(salaryConfig, allocations) { config, allocs ->
                Pair(config, allocs)
            }.collect { (config, allocs) ->
                checkAlerts(config, allocs)
            }
        }
    }

    private suspend fun checkAlerts(config: SalaryConfig?, allocations: List<BudgetAllocation>) {
        val salary = config?.monthlySalary ?: return
        if (salary <= 0) return

        val now = Calendar.getInstance()
        val month = now.get(Calendar.MONTH)
        val year = now.get(Calendar.YEAR)
        val currentMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val expenses = repository.getExpensesByMonth(month, year).first()
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        val alertList = mutableListOf<AlertItem>()
        var totalSpent = 0.0

        allocations.forEach { alloc ->
            val budget = salary * (alloc.allocatedPercent / 100.0)
            val spent = categoryTotals[alloc.category] ?: 0.0
            totalSpent += spent

            val percentUsed = if (budget > 0) (spent / budget).toFloat() else 0f
            val severity = when {
                percentUsed > 1.0f -> AlertSeverity.CRITICAL
                percentUsed > 0.8f -> AlertSeverity.WARNING
                else -> AlertSeverity.NORMAL
            }

            if (severity != AlertSeverity.NORMAL) {
                alertList.add(
                    AlertItem(
                        category = alloc.category,
                        spent = spent,
                        limit = budget,
                        percentUsed = percentUsed,
                        severity = severity
                    )
                )

                // Send notification only once per hour for each category
                if (currentMinute / 60 != lastCheckedMinute / 60) {
                    NotificationHelper.showBudgetAlert(
                        context = appContext,
                        category = alloc.category,
                        spent = spent,
                        limit = budget,
                        percentUsed = percentUsed
                    )
                }
            }
        }

        if (totalSpent > salary && currentMinute / 60 != lastCheckedMinute / 60) {
            NotificationHelper.showOverBudgetNotification(
                context = appContext,
                totalSpent = totalSpent,
                salary = salary
            )
        }

        lastCheckedMinute = currentMinute
        _alerts.value = alertList.sortedByDescending { it.percentUsed }
        _totalSpent.value = totalSpent
        _totalBudget.value = salary
        _isOverBudget.value = totalSpent > salary
    }

    fun dismissAlert(category: String) {
        _alerts.value = _alerts.value.filter { it.category != category }
    }
}
