package com.financeapp.ui.screens.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.BudgetAllocation
import com.financeapp.data.CategoryTotal
import com.financeapp.data.Expense
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import com.financeapp.data.GoalMilestone
import com.financeapp.data.SalaryConfig
import com.financeapp.data.SavingsGoal
import com.financeapp.util.ExportUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class MonthlyInsight(
    val month: String,
    val totalExpenses: Double,
    val savings: Double,
    val topCategory: String,
    val topCategoryAmount: Double
)

data class SpendingTrend(
    val category: String,
    val thisMonth: Double,
    val lastMonth: Double,
    val change: Double,
    val changePercent: Double
)

data class SalaryVsExpenses(
    val salary: Double,
    val totalExpenses: Double,
    val categoryBreakdown: List<CategoryBudgetComparison>,
    val overspentCategories: List<String>
)

data class CategoryBudgetComparison(
    val category: String,
    val budget: Double,
    val actual: Double,
    val percentUsed: Float,
    val isOverBudget: Boolean
)

data class SavingsGrowthPoint(
    val month: String,
    val targetSavings: Double,
    val actualSavings: Double,
    val cumulativeSavings: Double
)

data class WeeklySpendingDay(
    val dayName: String,
    val amount: Double,
    val dayOfWeek: Int
)

data class MilestoneProgressData(
    val goalName: String,
    val totalMilestones: Int,
    val completedMilestones: Int,
    val totalTarget: Double,
    val totalCurrent: Double,
    val progress: Float
)

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository
    private val appContext = application

    val salaryConfig: StateFlow<SalaryConfig?>
    val goals: StateFlow<List<SavingsGoal>>

    private val _selectedPeriod = MutableStateFlow("monthly")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    private val _categoryTotals = MutableStateFlow<List<CategoryTotal>>(emptyList())
    val categoryTotals: StateFlow<List<CategoryTotal>> = _categoryTotals.asStateFlow()

    private val _insights = MutableStateFlow<List<MonthlyInsight>>(emptyList())
    val insights: StateFlow<List<MonthlyInsight>> = _insights.asStateFlow()

    private val _trends = MutableStateFlow<List<SpendingTrend>>(emptyList())
    val trends: StateFlow<List<SpendingTrend>> = _trends.asStateFlow()

    private val _salaryVsExpenses = MutableStateFlow<SalaryVsExpenses?>(null)
    val salaryVsExpenses: StateFlow<SalaryVsExpenses?> = _salaryVsExpenses.asStateFlow()

    private val _savingsGrowth = MutableStateFlow<List<SavingsGrowthPoint>>(emptyList())
    val savingsGrowth: StateFlow<List<SavingsGrowthPoint>> = _savingsGrowth.asStateFlow()

    private val _weeklySpending = MutableStateFlow<List<WeeklySpendingDay>>(emptyList())
    val weeklySpending: StateFlow<List<WeeklySpendingDay>> = _weeklySpending.asStateFlow()

    private val _milestoneProgress = MutableStateFlow<List<MilestoneProgressData>>(emptyList())
    val milestoneProgress: StateFlow<List<MilestoneProgressData>> = _milestoneProgress.asStateFlow()

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()

    private val _totalBudget = MutableStateFlow(0.0)
    val totalBudget: StateFlow<Double> = _totalBudget.asStateFlow()

    private val _exportUri = MutableStateFlow<android.net.Uri?>(null)
    val exportUri: StateFlow<android.net.Uri?> = _exportUri.asStateFlow()

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        salaryConfig = repository.getSalaryConfig()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        goals = repository.getAllSavingsGoals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            _selectedPeriod.collect { period ->
                loadDataForPeriod(period)
            }
        }

        viewModelScope.launch {
            loadInsights()
            loadSavingsGrowth()
            loadWeeklySpending()
            loadMilestoneProgress()
        }
    }

    private suspend fun loadDataForPeriod(period: String) {
        val now = Calendar.getInstance()

        when (period) {
            "weekly" -> {
                val weekStart = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val allExpenses = repository.getAllExpenses().first()
                val weekExpenses = allExpenses.filter { it.date >= weekStart }
                _categoryTotals.value = weekExpenses.groupBy { it.category }
                    .map { (cat, list) -> CategoryTotal(cat, list.sumOf { it.amount }) }
                    .sortedByDescending { it.total }
                _totalExpenses.value = weekExpenses.sumOf { it.amount }
            }
            "monthly" -> {
                val month = now.get(Calendar.MONTH)
                val year = now.get(Calendar.YEAR)
                val cats = repository.getCategoryTotals(month, year).first()
                _categoryTotals.value = cats
                _totalExpenses.value = cats.sumOf { it.total }
            }
            "yearly" -> {
                val year = now.get(Calendar.YEAR)
                var total = 0.0
                val allCats = mutableMapOf<String, Double>()
                for (m in 0..11) {
                    val cats = repository.getCategoryTotals(m, year).first()
                    cats.forEach { cat ->
                        allCats[cat.category] = (allCats[cat.category] ?: 0.0) + cat.total
                    }
                    total += cats.sumOf { it.total }
                }
                _categoryTotals.value = allCats.map { (cat, total) -> CategoryTotal(cat, total) }
                    .sortedByDescending { it.total }
                _totalExpenses.value = total
            }
        }

        loadSalaryVsExpenses()
        loadTrends()
    }

    private suspend fun loadSalaryVsExpenses() {
        val config = salaryConfig.value
        val salary = config?.monthlySalary ?: 0.0
        val allocs = repository.getAllAllocations().first()
        val cats = _categoryTotals.value

        val salaryMap = allocs.associate { it.category to (salary * it.allocatedPercent / 100.0) }
        val actualMap = cats.associate { it.category to it.total }

        val breakdown = (salaryMap.keys + actualMap.keys).distinct().map { category ->
            val budget = salaryMap[category] ?: 0.0
            val actual = actualMap[category] ?: 0.0
            val percentUsed = if (budget > 0) (actual / budget).toFloat() else 0f
            CategoryBudgetComparison(
                category = category,
                budget = budget,
                actual = actual,
                percentUsed = percentUsed,
                isOverBudget = actual > budget && budget > 0
            )
        }.sortedByDescending { it.percentUsed }

        val overspent = breakdown.filter { it.isOverBudget }.map { it.category }

        _salaryVsExpenses.value = SalaryVsExpenses(
            salary = salary,
            totalExpenses = _totalExpenses.value,
            categoryBreakdown = breakdown,
            overspentCategories = overspent
        )

        _totalBudget.value = salary
    }

    private suspend fun loadTrends() {
        val now = Calendar.getInstance()
        val thisMonth = now.get(Calendar.MONTH)
        val thisYear = now.get(Calendar.YEAR)

        val lastCal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val lastMonth = lastCal.get(Calendar.MONTH)
        val lastYear = lastCal.get(Calendar.YEAR)

        try {
            val thisMonthExpenses = repository.getExpensesByMonth(thisMonth, thisYear).first()
            val lastMonthExpenses = repository.getExpensesByMonth(lastMonth, lastYear).first()

            val thisMonthTotals = thisMonthExpenses.groupBy { it.category }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
            val lastMonthTotals = lastMonthExpenses.groupBy { it.category }
                .mapValues { (_, list) -> list.sumOf { it.amount } }

            val allCategories = (thisMonthTotals.keys + lastMonthTotals.keys).distinct()

            val trends = allCategories.map { category ->
                val thisAmount = thisMonthTotals[category] ?: 0.0
                val lastAmount = lastMonthTotals[category] ?: 0.0
                val change = thisAmount - lastAmount
                val changePercent = if (lastAmount > 0) (change / lastAmount) * 100
                else if (thisAmount > 0) 100.0 else 0.0

                SpendingTrend(
                    category = category,
                    thisMonth = thisAmount,
                    lastMonth = lastAmount,
                    change = change,
                    changePercent = changePercent
                )
            }.sortedByDescending { kotlin.math.abs(it.change) }

            _trends.value = trends
        } catch (_: Exception) {}
    }

    private suspend fun loadInsights() {
        val insights = mutableListOf<MonthlyInsight>()

        for (i in 0 downTo 5) {
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            val monthName = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.US).format(cal.time)

            try {
                val monthExpenses = repository.getExpensesByMonth(month, year).first()
                val total = monthExpenses.sumOf { it.amount }
                val salary = salaryConfig.value?.monthlySalary ?: 0.0
                val savings = salary - total

                val topCat = monthExpenses.groupBy { it.category }
                    .maxByOrNull { it.value.sumOf { e -> e.amount } }

                insights.add(
                    MonthlyInsight(
                        month = monthName,
                        totalExpenses = total,
                        savings = savings,
                        topCategory = topCat?.key ?: "None",
                        topCategoryAmount = topCat?.value?.sumOf { it.amount } ?: 0.0
                    )
                )
            } catch (_: Exception) {}
        }

        _insights.value = insights
    }

    private suspend fun loadSavingsGrowth() {
        val growth = mutableListOf<SavingsGrowthPoint>()
        val salary = salaryConfig.value?.monthlySalary ?: 0.0
        val savingsPercent = salaryConfig.value?.savingsPercent ?: 20.0
        val targetSavings = salary * (savingsPercent / 100.0)
        var cumulativeTarget = 0.0
        var cumulativeActual = 0.0

        for (i in 5 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            val monthName = java.text.SimpleDateFormat("MMM", java.util.Locale.US).format(cal.time)

            try {
                val monthExpenses = repository.getExpensesByMonth(month, year).first()
                val totalExpenses = monthExpenses.sumOf { it.amount }
                val actualSavings = maxOf(0.0, salary - totalExpenses)

                cumulativeTarget += targetSavings
                cumulativeActual += actualSavings

                growth.add(
                    SavingsGrowthPoint(
                        month = monthName,
                        targetSavings = cumulativeTarget,
                        actualSavings = actualSavings,
                        cumulativeSavings = cumulativeActual
                    )
                )
            } catch (_: Exception) {}
        }

        _savingsGrowth.value = growth
    }

    private suspend fun loadWeeklySpending() {
        val now = Calendar.getInstance()
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        val weekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        try {
            val allExpenses = repository.getAllExpenses().first()
            val weekExpenses = allExpenses.filter { it.date >= weekStart }

            val dailyTotals = weekExpenses.groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                cal.get(Calendar.DAY_OF_WEEK)
            }.mapValues { (_, list) -> list.sumOf { it.amount } }

            val weeklyData = (1..7).map { dow ->
                WeeklySpendingDay(
                    dayName = dayNames[dow - 1],
                    amount = dailyTotals[dow] ?: 0.0,
                    dayOfWeek = dow
                )
            }

            _weeklySpending.value = weeklyData
        } catch (_: Exception) {}
    }

    private suspend fun loadMilestoneProgress() {
        val allGoals = goals.value
        val progressData = mutableListOf<MilestoneProgressData>()

        allGoals.forEach { goal ->
            val milestones = repository.getMilestonesForGoalSync(goal.id)
            if (milestones.isNotEmpty()) {
                val totalTarget = milestones.sumOf { it.targetAmount }
                val totalCurrent = milestones.sumOf { it.currentAmount }
                val completedCount = milestones.count { it.isCompleted }
                val progress = if (totalTarget > 0) (totalCurrent / totalTarget).toFloat() else 0f

                progressData.add(
                    MilestoneProgressData(
                        goalName = goal.name,
                        totalMilestones = milestones.size,
                        completedMilestones = completedCount,
                        totalTarget = totalTarget,
                        totalCurrent = totalCurrent,
                        progress = progress
                    )
                )
            }
        }

        _milestoneProgress.value = progressData
    }

    fun setPeriod(period: String) {
        _selectedPeriod.value = period
    }

    fun exportExpensesCsv() {
        viewModelScope.launch {
            val allExpenses = repository.getAllExpenses().first()
            val uri = ExportUtils.exportExpensesToCsv(appContext, allExpenses)
            _exportUri.value = uri
            _exportMessage.value = if (uri != null) "Expenses exported successfully!" else "Export failed."
        }
    }

    fun exportGoalsCsv() {
        viewModelScope.launch {
            val allGoals = goals.value
            val uri = ExportUtils.exportGoalsToCsv(appContext, allGoals)
            _exportUri.value = uri
            _exportMessage.value = if (uri != null) "Goals exported successfully!" else "Export failed."
        }
    }

    fun exportFullReport() {
        viewModelScope.launch {
            val now = Calendar.getInstance()
            val month = now.get(Calendar.MONTH)
            val year = now.get(Calendar.YEAR)
            val salary = salaryConfig.value?.monthlySalary ?: 0.0

            val monthExpenses = repository.getExpensesByMonth(month, year).first()
            val uri = ExportUtils.exportFullReport(
                context = appContext,
                expenses = monthExpenses,
                goals = goals.value,
                alerts = emptyList(),
                salary = salary,
                month = month,
                year = year
            )
            _exportUri.value = uri
            _exportMessage.value = if (uri != null) "Full report exported!" else "Export failed."
        }
    }

    fun shareExport() {
        _exportUri.value?.let { uri ->
            ExportUtils.shareFile(appContext, uri)
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }
}
