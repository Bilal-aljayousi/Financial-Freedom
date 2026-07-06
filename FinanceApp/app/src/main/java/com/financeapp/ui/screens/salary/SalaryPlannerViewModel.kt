package com.financeapp.ui.screens.salary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.BudgetAllocation
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import com.financeapp.data.SalaryConfig
import com.financeapp.util.BudgetGroup
import com.financeapp.util.SavingsOptimizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SalaryPlannerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    private val _editingSalary = MutableStateFlow("")
    val editingSalary: StateFlow<String> = _editingSalary.asStateFlow()

    private val _editingNeedsPercent = MutableStateFlow("")
    val editingNeedsPercent: StateFlow<String> = _editingNeedsPercent.asStateFlow()

    private val _editingWantsPercent = MutableStateFlow("")
    val editingWantsPercent: StateFlow<String> = _editingWantsPercent.asStateFlow()

    private val _editingSavingsPercent = MutableStateFlow("")
    val editingSavingsPercent: StateFlow<String> = _editingSavingsPercent.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _showEditAllocation = MutableStateFlow<BudgetAllocation?>(null)
    val showEditAllocation: StateFlow<BudgetAllocation?> = _showEditAllocation.asStateFlow()

    val salaryConfig: StateFlow<SalaryConfig?>
    val allocations: StateFlow<List<BudgetAllocation>>
    val budgetGroups: StateFlow<List<BudgetGroup>>

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        salaryConfig = repository.getSalaryConfig()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        allocations = repository.getAllAllocations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        budgetGroups = combine(salaryConfig, allocations) { config, allocs ->
            val cfg = config ?: SalaryConfig()
            SavingsOptimizer.buildBudgetGroups(cfg, allocs)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun startEditing() {
        val config = salaryConfig.value ?: SalaryConfig()
        _editingSalary.value = if (config.monthlySalary > 0) config.monthlySalary.toString() else ""
        _editingNeedsPercent.value = config.needsPercent.toString()
        _editingWantsPercent.value = config.wantsPercent.toString()
        _editingSavingsPercent.value = config.savingsPercent.toString()
        _isEditing.value = true
    }

    fun cancelEditing() {
        _isEditing.value = false
    }

    fun updateSalary(salary: String) {
        _editingSalary.value = salary
    }

    fun updateNeedsPercent(percent: String) {
        _editingNeedsPercent.value = percent
    }

    fun updateWantsPercent(percent: String) {
        _editingWantsPercent.value = percent
    }

    fun updateSavingsPercent(percent: String) {
        _editingSavingsPercent.value = percent
    }

    fun saveConfig() {
        val salary = _editingSalary.value.toDoubleOrNull() ?: 0.0
        val needs = _editingNeedsPercent.value.toDoubleOrNull() ?: 50.0
        val wants = _editingWantsPercent.value.toDoubleOrNull() ?: 30.0
        val savings = _editingSavingsPercent.value.toDoubleOrNull() ?: 20.0

        val total = needs + wants + savings
        val normalizedNeeds = if (total > 0) (needs / total) * 100 else 50.0
        val normalizedWants = if (total > 0) (wants / total) * 100 else 30.0
        val normalizedSavings = if (total > 0) (savings / total) * 100 else 20.0

        viewModelScope.launch {
            val config = SalaryConfig(
                id = 1,
                monthlySalary = salary,
                needsPercent = normalizedNeeds,
                wantsPercent = normalizedWants,
                savingsPercent = normalizedSavings
            )
            repository.updateSalaryConfig(config)
            _isEditing.value = false
        }
    }

    fun showEditAllocation(allocation: BudgetAllocation) {
        _showEditAllocation.value = allocation
    }

    fun dismissEditAllocation() {
        _showEditAllocation.value = null
    }

    fun saveAllocation(allocation: BudgetAllocation, newPercent: Double) {
        viewModelScope.launch {
            repository.updateAllocation(allocation.copy(allocatedPercent = newPercent))
            _showEditAllocation.value = null
        }
    }
}
