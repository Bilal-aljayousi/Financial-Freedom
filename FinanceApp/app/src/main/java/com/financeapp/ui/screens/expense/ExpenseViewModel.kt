package com.financeapp.ui.screens.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.BudgetAllocation
import com.financeapp.data.CategoryTotal
import com.financeapp.data.Expense
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import com.financeapp.data.SalaryConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class PeriodTotals(
    val daily: Double,
    val weekly: Double,
    val monthly: Double,
    val dailyBudget: Double,
    val weeklyBudget: Double,
    val monthlyBudget: Double
)

data class CategoryComparison(
    val category: String,
    val spent: Double,
    val budget: Double,
    val isOverBudget: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedPeriod = MutableStateFlow("monthly")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    val expenses: StateFlow<List<Expense>>
    val monthlyTotal: StateFlow<Double?>
    val categoryTotals: StateFlow<List<CategoryTotal>>
    val salaryConfig: StateFlow<SalaryConfig?>
    val allocations: StateFlow<List<BudgetAllocation>>

    private val _periodTotals = MutableStateFlow(PeriodTotals(0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
    val periodTotals: StateFlow<PeriodTotals> = _periodTotals.asStateFlow()

    private val _categoryComparisons = MutableStateFlow<List<CategoryComparison>>(emptyList())
    val categoryComparisons: StateFlow<List<CategoryComparison>> = _categoryComparisons.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        // Reactive flows that update when month/year changes
        expenses = combine(_selectedMonth, _selectedYear) { month, year ->
            Pair(month, year)
        }.flatMapLatest { (month, year) ->
            repository.getExpensesByMonth(month, year)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        monthlyTotal = combine(_selectedMonth, _selectedYear) { month, year ->
            Pair(month, year)
        }.flatMapLatest { (month, year) ->
            repository.getMonthlyTotal(month, year)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        categoryTotals = combine(_selectedMonth, _selectedYear) { month, year ->
            Pair(month, year)
        }.flatMapLatest { (month, year) ->
            repository.getCategoryTotals(month, year)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        salaryConfig = repository.getSalaryConfig()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        allocations = repository.getAllAllocations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            combine(expenses, salaryConfig, allocations) { expenseList, config, allocs ->
                calculatePeriodBreakdown(expenseList, config, allocs)
            }.collect { comparisons ->
                _categoryComparisons.value = comparisons
            }
        }
    }

    private suspend fun calculatePeriodBreakdown(
        expenses: List<Expense>,
        salaryConfig: SalaryConfig?,
        allocations: List<BudgetAllocation>
    ): List<CategoryComparison> {
        val salary = salaryConfig?.monthlySalary ?: 0.0
        val periodTotals = calculatePeriodTotals(expenses, salary)
        _periodTotals.value = periodTotals

        return allocations.map { alloc ->
            val spent = expenses.filter { it.category == alloc.category }.sumOf { it.amount }
            val budget = salary * (alloc.allocatedPercent / 100.0)
            CategoryComparison(
                category = alloc.category,
                spent = spent,
                budget = budget,
                isOverBudget = spent > budget
            )
        }
    }

    private fun calculatePeriodTotals(expenses: List<Expense>, salary: Double): PeriodTotals {
        val now = Calendar.getInstance()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val weekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val daily = expenses.filter { it.date in todayStart..todayEnd }.sumOf { it.amount }
        val weekly = expenses.filter { it.date >= weekStart }.sumOf { it.amount }
        val monthly = expenses.sumOf { it.amount }

        val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        val weeksInMonth = daysInMonth / 7.0

        return PeriodTotals(
            daily = daily,
            weekly = weekly,
            monthly = monthly,
            dailyBudget = salary / daysInMonth,
            weeklyBudget = salary / weeksInMonth,
            monthlyBudget = salary
        )
    }

    fun addExpense(amount: Double, category: String, description: String) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                category = category,
                description = description,
                month = _selectedMonth.value,
                year = _selectedYear.value
            )
            repository.insertExpense(expense)
            _showAddDialog.value = false
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun setMonth(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun setPeriod(period: String) {
        _selectedPeriod.value = period
    }

    fun toggleAddDialog() {
        _showAddDialog.value = !_showAddDialog.value
    }
}
