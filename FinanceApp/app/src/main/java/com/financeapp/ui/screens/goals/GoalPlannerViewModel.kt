package com.financeapp.ui.screens.goals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import com.financeapp.data.GoalMilestone
import com.financeapp.data.SalaryConfig
import com.financeapp.data.SavingsGoal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class GoalWithMilestones(
    val goal: SavingsGoal,
    val milestones: List<GoalMilestone>,
    val milestoneTargetSum: Double,
    val milestoneCurrentSum: Double,
    val completedMilestoneCount: Int,
    val totalMilestoneCount: Int,
    val isAtRisk: Boolean,
    val estimatedCompletionMonths: Int,
    val monthlyAllocation: Double
)

data class GoalPlannerSummary(
    val totalGoals: Int,
    val activeGoals: Int,
    val completedGoals: Int,
    val totalMilestones: Int,
    val completedMilestones: Int,
    val overallProgress: Float,
    val totalTargetAmount: Double,
    val totalCurrentAmount: Double,
    val monthlySavingsNeeded: Double,
    val salaryConfig: SalaryConfig?
)

data class MilestoneFormData(
    val title: String = "",
    val description: String = "",
    val targetAmount: String = "",
    val deadline: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
    val goalId: Long = 0
)

data class GoalFormData(
    val name: String = "",
    val description: String = "",
    val targetAmount: String = "",
    val deadline: Long = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000
)

@OptIn(ExperimentalCoroutinesApi::class)
class GoalPlannerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    val salaryConfig: StateFlow<SalaryConfig?>

    private val _selectedGoalId = MutableStateFlow<Long?>(null)
    val selectedGoalId: StateFlow<Long?> = _selectedGoalId.asStateFlow()

    val selectedGoalMilestones: StateFlow<List<GoalMilestone>>

    private val _showAddGoalDialog = MutableStateFlow(false)
    val showAddGoalDialog: StateFlow<Boolean> = _showAddGoalDialog.asStateFlow()

    private val _showAddMilestoneDialog = MutableStateFlow(false)
    val showAddMilestoneDialog: StateFlow<Boolean> = _showAddMilestoneDialog.asStateFlow()

    private val _goalFormData = MutableStateFlow(GoalFormData())
    val goalFormData: StateFlow<GoalFormData> = _goalFormData.asStateFlow()

    private val _milestoneFormData = MutableStateFlow(MilestoneFormData())
    val milestoneFormData: StateFlow<MilestoneFormData> = _milestoneFormData.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val goals: StateFlow<List<SavingsGoal>>
    val goalWithMilestones: StateFlow<List<GoalWithMilestones>>
    val summary: StateFlow<GoalPlannerSummary>

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        salaryConfig = repository.getSalaryConfig()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        goals = repository.getAllSavingsGoals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        selectedGoalMilestones = _selectedGoalId.flatMapLatest { goalId ->
            if (goalId != null) {
                repository.getMilestonesByGoalId(goalId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        goalWithMilestones = combine(goals, salaryConfig) { goalList, config ->
            buildGoalWithMilestones(goalList, config)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        summary = combine(goals, salaryConfig) { goalList, config ->
            buildSummary(goalList, config)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
            GoalPlannerSummary(0, 0, 0, 0, 0, 0f, 0.0, 0.0, 0.0, null)
        )
    }

    private suspend fun buildGoalWithMilestones(
        goals: List<SavingsGoal>,
        config: SalaryConfig?
    ): List<GoalWithMilestones> {
        val salary = config?.monthlySalary ?: 0.0
        val savingsPercent = config?.savingsPercent ?: 20.0
        val monthlySavings = salary * (savingsPercent / 100.0)

        return goals.map { goal ->
            val milestones = repository.getMilestonesForGoalSync(goal.id)
            val milestoneTargetSum = milestones.sumOf { it.targetAmount }
            val milestoneCurrentSum = milestones.sumOf { it.currentAmount }
            val completedCount = milestones.count { it.isCompleted }
            val totalCount = milestones.size

            val remaining = goal.targetAmount - goal.currentAmount
            val daysLeft = TimeUnit.MILLISECONDS.toDays(goal.deadline - System.currentTimeMillis())
            val monthsLeft = (daysLeft / 30.0).coerceAtLeast(1.0)
            val monthlyNeeded = if (remaining > 0) remaining / monthsLeft else 0.0
            val estimatedMonths = if (monthlyNeeded > 0) (remaining / monthlyNeeded).toInt() else 0

            val isAtRisk = daysLeft > 0 && estimatedMonths > monthsLeft

            val goalIndex = goals.indexOf(goal)
            val totalGoals = goals.size
            val monthlyAllocation = if (totalGoals > 0) monthlySavings / totalGoals else 0.0

            GoalWithMilestones(
                goal = goal,
                milestones = milestones,
                milestoneTargetSum = milestoneTargetSum,
                milestoneCurrentSum = milestoneCurrentSum,
                completedMilestoneCount = completedCount,
                totalMilestoneCount = totalCount,
                isAtRisk = isAtRisk,
                estimatedCompletionMonths = estimatedMonths,
                monthlyAllocation = monthlyAllocation
            )
        }
    }

    private suspend fun buildSummary(goals: List<SavingsGoal>, config: SalaryConfig?): GoalPlannerSummary {
        val totalGoals = goals.size
        val now = System.currentTimeMillis()
        val activeGoals = goals.count { it.deadline > now && it.currentAmount < it.targetAmount }
        val completedGoals = goals.count { it.currentAmount >= it.targetAmount }

        val totalTarget = goals.sumOf { it.targetAmount }
        val totalCurrent = goals.sumOf { it.currentAmount }
        val progress = if (totalTarget > 0) (totalCurrent / totalTarget).toFloat() else 0f

        val salary = config?.monthlySalary ?: 0.0
        val savingsPercent = config?.savingsPercent ?: 20.0
        val monthlySavings = salary * (savingsPercent / 100.0)

        val totalRemaining = goals.sumOf { (it.targetAmount - it.currentAmount).coerceAtLeast(0.0) }
        val avgMonthsLeft = goals.map {
            val days = TimeUnit.MILLISECONDS.toDays(it.deadline - now)
            (days / 30.0).coerceAtLeast(1.0)
        }.average().coerceAtLeast(1.0)
        val monthlyNeeded = if (totalRemaining > 0) totalRemaining / avgMonthsLeft else 0.0

        var totalMilestones = 0
        var completedMilestones = 0
        goals.forEach { goal ->
            val milestones = repository.getMilestonesForGoalSync(goal.id)
            totalMilestones += milestones.size
            completedMilestones += milestones.count { it.isCompleted }
        }

        return GoalPlannerSummary(
            totalGoals = totalGoals,
            activeGoals = activeGoals,
            completedGoals = completedGoals,
            totalMilestones = totalMilestones,
            completedMilestones = completedMilestones,
            overallProgress = progress,
            totalTargetAmount = totalTarget,
            totalCurrentAmount = totalCurrent,
            monthlySavingsNeeded = monthlyNeeded,
            salaryConfig = config
        )
    }

    fun selectGoal(goalId: Long?) {
        _selectedGoalId.value = goalId
    }

    fun showAddGoalDialog(show: Boolean) {
        _showAddGoalDialog.value = show
        if (show) _goalFormData.value = GoalFormData()
    }

    fun showAddMilestoneDialog(goalId: Long, show: Boolean) {
        _showAddMilestoneDialog.value = show
        if (show) _milestoneFormData.value = MilestoneFormData(goalId = goalId)
    }

    fun updateGoalForm(form: GoalFormData) {
        _goalFormData.value = form
    }

    fun updateMilestoneForm(form: MilestoneFormData) {
        _milestoneFormData.value = form
    }

    fun createGoal() {
        val form = _goalFormData.value
        val targetAmount = form.targetAmount.toDoubleOrNull()
        if (form.name.isBlank() || targetAmount == null || targetAmount <= 0) {
            _message.value = "Please fill in all fields correctly"
            return
        }

        viewModelScope.launch {
            val goal = SavingsGoal(
                name = form.name,
                targetAmount = targetAmount,
                deadline = form.deadline
            )
            repository.insertSavingsGoal(goal)
            _showAddGoalDialog.value = false
            _message.value = "Goal created successfully!"
        }
    }

    fun createMilestone() {
        val form = _milestoneFormData.value
        val targetAmount = form.targetAmount.toDoubleOrNull()
        if (form.title.isBlank() || targetAmount == null || targetAmount <= 0) {
            _message.value = "Please fill in all fields correctly"
            return
        }

        viewModelScope.launch {
            val milestones = repository.getMilestonesForGoalSync(form.goalId)
            val nextOrder = milestones.size

            val milestone = GoalMilestone(
                goalId = form.goalId,
                title = form.title,
                description = form.description,
                targetAmount = targetAmount,
                deadline = form.deadline,
                orderIndex = nextOrder
            )
            repository.insertMilestone(milestone)
            _showAddMilestoneDialog.value = false
            _message.value = "Milestone created successfully!"
        }
    }

    fun updateMilestoneAmount(milestoneId: Long, amount: Double) {
        viewModelScope.launch {
            val milestone = repository.getMilestoneByIdSync(milestoneId) ?: return@launch
            val newAmount = (milestone.currentAmount + amount).coerceAtLeast(0.0)
            repository.updateMilestoneAmount(milestoneId, newAmount)

            if (newAmount >= milestone.targetAmount && !milestone.isCompleted) {
                repository.updateMilestoneCompleted(milestoneId, true, System.currentTimeMillis())
                updateGoalProgress(milestone.goalId)
            }
        }
    }

    fun setMilestoneAmount(milestoneId: Long, amount: Double) {
        viewModelScope.launch {
            val milestone = repository.getMilestoneByIdSync(milestoneId) ?: return@launch
            repository.updateMilestoneAmount(milestoneId, amount.coerceAtLeast(0.0))

            if (amount >= milestone.targetAmount && !milestone.isCompleted) {
                repository.updateMilestoneCompleted(milestoneId, true, System.currentTimeMillis())
            } else if (amount < milestone.targetAmount && milestone.isCompleted) {
                repository.updateMilestoneCompleted(milestoneId, false, null)
            }
            updateGoalProgress(milestone.goalId)
        }
    }

    private suspend fun updateGoalProgress(goalId: Long) {
        val milestones = repository.getMilestonesForGoalSync(goalId)
        val totalCurrent = milestones.sumOf { it.currentAmount }
        repository.updateSavingsAmount(goalId, totalCurrent)
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
            repository.deleteAllMilestonesForGoal(goal.id)
            _message.value = "Goal deleted"
        }
    }

    fun deleteMilestone(milestone: GoalMilestone) {
        viewModelScope.launch {
            repository.deleteMilestone(milestone)
            updateGoalProgress(milestone.goalId)
            _message.value = "Milestone deleted"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
