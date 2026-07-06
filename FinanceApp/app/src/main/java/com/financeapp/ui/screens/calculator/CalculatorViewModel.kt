package com.financeapp.ui.screens.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CalculatorResult(
    val futureValue: Double,
    val totalContributions: Double,
    val totalInterest: Double,
    val yearlyBreakdown: List<Pair<Int, Double>>
)

class CalculatorViewModel : ViewModel() {
    private val _principal = MutableStateFlow("")
    val principal: StateFlow<String> = _principal.asStateFlow()

    private val _monthlyContribution = MutableStateFlow("")
    val monthlyContribution: StateFlow<String> = _monthlyContribution.asStateFlow()

    private val _annualRate = MutableStateFlow("")
    val annualRate: StateFlow<String> = _annualRate.asStateFlow()

    private val _years = MutableStateFlow("")
    val years: StateFlow<String> = _years.asStateFlow()

    private val _result = MutableStateFlow<CalculatorResult?>(null)
    val result: StateFlow<CalculatorResult?> = _result.asStateFlow()

    fun updatePrincipal(value: String) {
        _principal.value = value
    }

    fun updateMonthlyContribution(value: String) {
        _monthlyContribution.value = value
    }

    fun updateAnnualRate(value: String) {
        _annualRate.value = value
    }

    fun updateYears(value: String) {
        _years.value = value
    }

    fun calculate() {
        val p = _principal.value.toDoubleOrNull() ?: 0.0
        val m = _monthlyContribution.value.toDoubleOrNull() ?: 0.0
        val rate = (_annualRate.value.toDoubleOrNull() ?: 0.0) / 100
        val years = _years.value.toIntOrNull() ?: 0

        if (years <= 0 || (p <= 0 && m <= 0)) {
            _result.value = null
            return
        }

        val monthlyRate = rate / 12
        val totalMonths = years * 12
        val yearlyBreakdown = mutableListOf<Pair<Int, Double>>()
        var currentAmount = p
        var totalContributions = p

        for (month in 1..totalMonths) {
            currentAmount = currentAmount * (1 + monthlyRate) + m
            if (month % 12 == 0) {
                yearlyBreakdown.add(Pair(month / 12, currentAmount))
            }
            if (month % 12 == 0) {
                totalContributions += m * 12
            }
        }

        val totalInterest = currentAmount - totalContributions

        _result.value = CalculatorResult(
            futureValue = currentAmount,
            totalContributions = totalContributions,
            totalInterest = totalInterest,
            yearlyBreakdown = yearlyBreakdown
        )
    }

    fun clear() {
        _principal.value = ""
        _monthlyContribution.value = ""
        _annualRate.value = ""
        _years.value = ""
        _result.value = null
    }
}
