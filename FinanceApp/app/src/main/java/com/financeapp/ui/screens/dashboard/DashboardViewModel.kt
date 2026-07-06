package com.financeapp.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.BudgetAllocation
import com.financeapp.data.CategoryTotal
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import com.financeapp.data.SalaryConfig
import com.financeapp.data.SavingsGoal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class OverspendingCategory(
    val category: String,
    val budget: Double,
    val actual: Double,
    val percentOver: Float
)

data class DashboardSummary(
    val monthlyIncome: Double,
    val totalExpenses: Double,
    val netSavings: Double,
    val savingsRate: Double,
    val totalGoalProgress: Float,
    val activeGoalCount: Int,
    val atRiskGoalCount: Int,
    val topExpenseCategory: String,
    val topExpenseAmount: Double,
    val overspendingCategories: List<OverspendingCategory> = emptyList(),
    val budgetUtilization: Float = 0f,
    val totalMilestones: Int = 0,
    val completedMilestones: Int = 0,
    val totalMilestoneTarget: Double = 0.0,
    val totalMilestoneCurrent: Double = 0.0
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    val salaryConfig: StateFlow<SalaryConfig?>
    val goals: StateFlow<List<SavingsGoal>>
    val categoryTotals: StateFlow<List<CategoryTotal>>

    private val _summary = MutableStateFlow(
        DashboardSummary(0.0, 0.0, 0.0, 0.0, 0f, 0, 0, "None", 0.0)
    )
    val summary: StateFlow<DashboardSummary> = _summary.asStateFlow()

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        salaryConfig = repository.getSalaryConfig()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        goals = repository.getAllSavingsGoals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        val now = Calendar.getInstance()
        val month = now.get(Calendar.MONTH)
        val year = now.get(Calendar.YEAR)

        categoryTotals = repository.getCategoryTotals(month, year)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            combine(salaryConfig, goals, categoryTotals) { config, goalList, cats ->
                buildSummary(config, goalList, cats)
            }.collect { summary ->
                _summary.value = summary
            }
        }
    }

    private suspend fun buildSummary(
        config: SalaryConfig?,
        goals: List<SavingsGoal>,
        categoryTotals: List<CategoryTotal>
    ): DashboardSummary {
        val salary = config?.monthlySalary ?: 0.0
        val totalExpenses = categoryTotals.sumOf { it.total }
        val netSavings = salary - totalExpenses
        val savingsRate = if (salary > 0) (netSavings / salary) * 100 else 0.0

        val totalTarget = goals.sumOf { it.targetAmount }
        val totalCurrent = goals.sumOf { it.currentAmount }
        val goalProgress = if (totalTarget > 0) (totalCurrent / totalTarget).toFloat() else 0f

        val now = Calendar.getInstance()
        val atRiskCount = goals.count { goal ->
            val daysLeft = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(
                goal.deadline - System.currentTimeMillis()
            )
            val remaining = goal.targetAmount - goal.currentAmount
            val monthlyNeeded = if (daysLeft > 30) remaining / (daysLeft / 30.0) else remaining
            val estimatedMonths = if (monthlyNeeded > 0) (remaining / monthlyNeeded).toInt() else 999
            val monthsLeft = (daysLeft / 30).toInt()
            daysLeft > 0 && estimatedMonths > monthsLeft
        }

        val topCat = categoryTotals.maxByOrNull { it.total }

        // Calculate overspending
        val allocs = repository.getAllAllocations().first()
        val overspending = mutableListOf<OverspendingCategory>()
        var totalBudget = 0.0
        var totalUsed = 0.0

        allocs.forEach { alloc ->
            val budget = salary * (alloc.allocatedPercent / 100.0)
            totalBudget += budget
            val catTotal = categoryTotals.find { it.category == alloc.category }?.total ?: 0.0
            totalUsed += catTotal

            if (catTotal > budget && budget > 0) {
                val percentOver = ((catTotal - budget) / budget * 100).toFloat()
                overspending.add(
                    OverspendingCategory(
                        category = alloc.category,
                        budget = budget,
                        actual = catTotal,
                        percentOver = percentOver
                    )
                )
            }
        }

        val budgetUtilization = if (totalBudget > 0) (totalUsed / totalBudget).toFloat() else 0f

        // Calculate milestone data
        var totalMilestones = 0
        var completedMilestones = 0
        var totalMilestoneTarget = 0.0
        var totalMilestoneCurrent = 0.0

        goals.forEach { goal ->
            val milestones = repository.getMilestonesForGoalSync(goal.id)
            totalMilestones += milestones.size
            completedMilestones += milestones.count { it.isCompleted }
            totalMilestoneTarget += milestones.sumOf { it.targetAmount }
            totalMilestoneCurrent += milestones.sumOf { it.currentAmount }
        }

        return DashboardSummary(
            monthlyIncome = salary,
            totalExpenses = totalExpenses,
            netSavings = netSavings,
            savingsRate = savingsRate,
            totalGoalProgress = goalProgress,
            activeGoalCount = goals.size,
            atRiskGoalCount = atRiskCount,
            topExpenseCategory = topCat?.category ?: "None",
            topExpenseAmount = topCat?.total ?: 0.0,
            overspendingCategories = overspending.sortedByDescending { it.percentOver },
            budgetUtilization = budgetUtilization,
            totalMilestones = totalMilestones,
            completedMilestones = completedMilestones,
            totalMilestoneTarget = totalMilestoneTarget,
            totalMilestoneCurrent = totalMilestoneCurrent
        )
    }
}
