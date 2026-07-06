package com.financeapp.ui.screens.goals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import com.financeapp.data.SalaryConfig
import com.financeapp.data.SavingsGoal
import com.financeapp.notifications.NotificationHelper
import com.financeapp.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class GoalWithProgress(
    val goal: SavingsGoal,
    val progress: Float,
    val monthlyNeeded: Double,
    val estimatedMonths: Int,
    val isAtRisk: Boolean,
    val daysRemaining: Long
)

data class SavingsPotential(
    val actualSavings: Double,
    val maxSavings: Double,
    val potentialPercent: Float,
    val overspendAmount: Double,
    val suggestedSavings: Double
)

class GoalDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository
    private val appContext = application

    private val _selectedPeriod = MutableStateFlow("monthly")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    val goals: StateFlow<List<SavingsGoal>>
    val goalsWithProgress: StateFlow<List<GoalWithProgress>>
    val salaryConfig: StateFlow<SalaryConfig?>

    private val _savingsPotential = MutableStateFlow(
        SavingsPotential(0.0, 0.0, 0f, 0.0, 0.0)
    )
    val savingsPotential: StateFlow<SavingsPotential> = _savingsPotential.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow<SavingsGoal?>(null)
    val showUpdateDialog: StateFlow<SavingsGoal?> = _showUpdateDialog.asStateFlow()

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        goals = repository.getAllSavingsGoals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        salaryConfig = repository.getSalaryConfig()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        goalsWithProgress = combine(goals) { goalList ->
            goalList.map { goal -> buildGoalWithProgress(goal) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            combine(goals, salaryConfig) { goalList, config ->
                Pair(goalList, config)
            }.collect { (goalList, config) ->
                calculateSavingsPotential(goalList, config)
                checkGoalAlerts(goalList)
            }
        }
    }

    private fun checkGoalAlerts(goals: List<SavingsGoal>) {
        goals.forEach { goal ->
            val daysRemaining = TimeUtils.daysUntil(goal.deadline)
            val remaining = goal.targetAmount - goal.currentAmount

            if (remaining > 0) {
                val monthsRemaining = TimeUtils.monthsUntil(goal.deadline)
                val monthlyNeeded = if (monthsRemaining > 0) remaining / monthsRemaining else remaining
                val estimatedMonths = if (monthlyNeeded > 0) {
                    (remaining / monthlyNeeded).toInt()
                } else 999

                if (daysRemaining <= 0 || estimatedMonths > monthsRemaining) {
                    NotificationHelper.showSavingsAlert(
                        context = appContext,
                        goalName = goal.name,
                        currentAmount = goal.currentAmount,
                        targetAmount = goal.targetAmount,
                        daysRemaining = daysRemaining
                    )
                }
            }
        }
    }

    private fun calculateSavingsPotential(goals: List<SavingsGoal>, config: SalaryConfig?) {
        val salary = config?.monthlySalary ?: 0.0
        val now = Calendar.getInstance()
        val month = now.get(Calendar.MONTH)
        val year = now.get(Calendar.YEAR)

        viewModelScope.launch {
            try {
                val allExpenses = repository.getExpensesByMonth(month, year).first()
                val totalExpenses = allExpenses.sumOf { it.amount }

                val actualSavings = maxOf(0.0, salary - totalExpenses)
                val maxSavings = salary * ((config?.savingsPercent ?: 20.0) / 100.0)
                val potentialPercent = if (maxSavings > 0) {
                    (actualSavings / maxSavings).toFloat().coerceIn(0f, 1f)
                } else 0f
                val overspendAmount = maxOf(0.0, totalExpenses - salary)
                val suggestedSavings = maxOf(0.0, maxSavings - actualSavings)

                _savingsPotential.value = SavingsPotential(
                    actualSavings = actualSavings,
                    maxSavings = maxSavings,
                    potentialPercent = potentialPercent,
                    overspendAmount = overspendAmount,
                    suggestedSavings = suggestedSavings
                )
            } catch (_: Exception) {
                val maxSavings = salary * ((config?.savingsPercent ?: 20.0) / 100.0)
                _savingsPotential.value = SavingsPotential(
                    actualSavings = salary,
                    maxSavings = maxSavings,
                    potentialPercent = 1f,
                    overspendAmount = 0.0,
                    suggestedSavings = 0.0
                )
            }
        }
    }

    private fun buildGoalWithProgress(goal: SavingsGoal): GoalWithProgress {
        val progress = if (goal.targetAmount > 0) {
            (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
        } else 0f

        val remaining = goal.targetAmount - goal.currentAmount
        val monthsRemaining = TimeUtils.monthsUntil(goal.deadline)
        val daysRemaining = TimeUtils.daysUntil(goal.deadline)

        val monthlyNeeded = if (monthsRemaining > 0) remaining / monthsRemaining else remaining
        val estimatedMonths = if (monthlyNeeded > 0) {
            (remaining / monthlyNeeded).toInt().coerceAtLeast(1)
        } else 999

        val isAtRisk = daysRemaining > 0 && estimatedMonths > monthsRemaining

        return GoalWithProgress(
            goal = goal,
            progress = progress,
            monthlyNeeded = monthlyNeeded,
            estimatedMonths = estimatedMonths,
            isAtRisk = isAtRisk,
            daysRemaining = daysRemaining
        )
    }

    fun setPeriod(period: String) {
        _selectedPeriod.value = period
    }

    fun addGoal(name: String, targetAmount: Double, deadline: Long) {
        viewModelScope.launch {
            val goal = SavingsGoal(
                name = name,
                targetAmount = targetAmount,
                deadline = deadline
            )
            repository.insertSavingsGoal(goal)
            _showAddDialog.value = false
        }
    }

    fun updateGoalAmount(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch {
            val newAmount = (goal.currentAmount + amount).coerceAtMost(goal.targetAmount)
            repository.updateSavingsAmount(goal.id, newAmount)
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    fun toggleAddDialog() {
        _showAddDialog.value = !_showAddDialog.value
    }

    fun showUpdateDialog(goal: SavingsGoal) {
        _showUpdateDialog.value = goal
    }

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = null
    }
}
