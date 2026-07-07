package com.financeapp.ui.screens.goals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import com.financeapp.data.SavingsGoal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    val goals: StateFlow<List<SavingsGoal>>

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        goals = repository.getAllGoals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addGoal(name: String, targetAmount: Double, deadline: Long) {
        viewModelScope.launch {
            repository.insertGoal(
                SavingsGoal(
                    name = name,
                    targetAmount = targetAmount,
                    deadline = deadline
                )
            )
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }
}
