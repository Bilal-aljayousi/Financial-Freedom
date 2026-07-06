package com.financeapp.ui.screens.savings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import com.financeapp.data.SavingsGoal
import com.financeapp.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    val goals: StateFlow<List<SavingsGoal>>

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        goals = repository.getAllSavingsGoals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
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

    fun updateGoalAmount(goal: SavingsGoal, additionalAmount: Double) {
        viewModelScope.launch {
            val newAmount = goal.currentAmount + additionalAmount
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

    fun calculateMonthlySavings(goal: SavingsGoal): Double {
        val monthsLeft = TimeUtils.monthsUntil(goal.deadline)
        if (monthsLeft <= 0) return 0.0
        val remaining = goal.targetAmount - goal.currentAmount
        return remaining / monthsLeft
    }
}
