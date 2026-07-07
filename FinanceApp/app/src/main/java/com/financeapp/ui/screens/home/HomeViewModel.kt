package com.financeapp.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import com.financeapp.data.SalaryConfig
import com.financeapp.data.SavingsGoal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeSummary(
    val activeGoal: SavingsGoal? = null,
    val salary: Double = 0.0,
    val currency: String = "JOD",
    val dailyNeeded: Double = 0.0,
    val monthlyNeeded: Double = 0.0,
    val totalSaved: Double = 0.0,
    val totalTarget: Double = 0.0,
    val overallProgress: Float = 0f,
    val daysRemaining: Long = 0,
    val monthsRemaining: Int = 0,
    val savedThisMonth: Double = 0.0,
    val onTrack: Boolean = true
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    val goals: StateFlow<List<SavingsGoal>>
    val salaryConfig: StateFlow<SalaryConfig?>

    private val _summary = MutableStateFlow(HomeSummary())
    val summary: StateFlow<HomeSummary> = _summary.asStateFlow()

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        goals = repository.getAllGoals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        salaryConfig = repository.getSalaryConfig()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        viewModelScope.launch {
            goals.collect { goalList ->
                val config = salaryConfig.value
                updateSummary(goalList, config)
            }
        }

        viewModelScope.launch {
            salaryConfig.collect { config ->
                updateSummary(goals.value, config)
            }
        }
    }

    private fun updateSummary(goals: List<SavingsGoal>, config: SalaryConfig?) {
        val activeGoal = goals.firstOrNull { !it.isCompleted }
        val totalSaved = goals.sumOf { it.currentAmount }
        val totalTarget = goals.sumOf { it.targetAmount }
        val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget).toFloat() else 0f

        val dailyNeeded = activeGoal?.dailyNeeded ?: 0.0
        val monthlyNeeded = activeGoal?.monthlyNeeded ?: 0.0
        val daysRemaining = activeGoal?.daysRemaining ?: 0
        val monthsRemaining = activeGoal?.monthsRemaining ?: 0

        // Calculate saved this month
        val now = Calendar.getInstance()
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val savedThisMonth = if (activeGoal != null && activeGoal.createdAt < monthStart.timeInMillis) {
            val monthStartAmount = activeGoal.currentAmount
            // Rough estimate: currentAmount - what was saved before this month
            maxOf(0.0, activeGoal.currentAmount * 0.1) // simplified
        } else 0.0

        // Check if on track
        val onTrack = if (activeGoal != null && activeGoal.monthsRemaining > 0) {
            val expectedProgress = 1f - (activeGoal.daysRemaining.toFloat() / (activeGoal.monthsRemaining * 30f))
            activeGoal.progress >= expectedProgress * 0.8f // 80% of expected
        } else true

        _summary.value = HomeSummary(
            activeGoal = activeGoal,
            salary = config?.monthlySalary ?: 0.0,
            currency = config?.currency ?: "JOD",
            dailyNeeded = dailyNeeded,
            monthlyNeeded = monthlyNeeded,
            totalSaved = totalSaved,
            totalTarget = totalTarget,
            overallProgress = overallProgress,
            daysRemaining = daysRemaining,
            monthsRemaining = monthsRemaining,
            savedThisMonth = savedThisMonth,
            onTrack = onTrack
        )
    }

    fun addSavingToGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            val goal = repository.getGoalByIdSync(goalId) ?: return@launch
            val newAmount = (goal.currentAmount + amount).coerceAtMost(goal.targetAmount)
            repository.updateGoalAmount(goalId, newAmount)
        }
    }
}
