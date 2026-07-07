package com.financeapp.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import com.financeapp.data.SalaryConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    val salaryConfig: StateFlow<SalaryConfig?>

    init {
        val db = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        salaryConfig = repository.getSalaryConfig()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun updateSalary(amount: Double, currency: String) {
        viewModelScope.launch {
            repository.updateSalaryConfig(
                SalaryConfig(
                    monthlySalary = amount,
                    currency = currency
                )
            )
        }
    }
}
