package com.financeapp.util

import com.financeapp.data.BudgetAllocation
import com.financeapp.data.SalaryConfig

data class BudgetGroup(
    val name: String,
    val percent: Double,
    val amount: Double,
    val allocations: List<BudgetAllocationDetail>
)

data class BudgetAllocationDetail(
    val category: String,
    val percent: Double,
    val amount: Double,
    val group: String
)

data class SavingsAnalysis(
    val monthlySalary: Double,
    val totalExpenses: Double,
    val actualSavings: Double,
    val maxSavingsPotential: Double,
    val savingsPotentialPercent: Double,
    val overspendAmount: Double,
    val suggestedReductions: List<SuggestedReduction>,
    val goalCompletionEstimates: Map<Long, String>
)

data class SuggestedReduction(
    val category: String,
    val currentSpending: Double,
    val recommendedLimit: Double,
    val potentialSaving: Double
)

object SavingsOptimizer {

    fun buildBudgetGroups(
        salaryConfig: SalaryConfig,
        allocations: List<BudgetAllocation>
    ): List<BudgetGroup> {
        val salary = salaryConfig.monthlySalary
        val groups = mapOf(
            "Needs" to salaryConfig.needsPercent,
            "Wants" to salaryConfig.wantsPercent,
            "Savings" to salaryConfig.savingsPercent
        )

        return groups.map { (groupName, groupPercent) ->
            val groupAllocations = allocations.filter { it.group == groupName }
            val groupAmount = salary * (groupPercent / 100.0)

            val details = groupAllocations.map { alloc ->
                BudgetAllocationDetail(
                    category = alloc.category,
                    percent = alloc.allocatedPercent,
                    amount = salary * (alloc.allocatedPercent / 100.0),
                    group = groupName
                )
            }

            BudgetGroup(
                name = groupName,
                percent = groupPercent,
                amount = groupAmount,
                allocations = details
            )
        }
    }

    fun calculateSavingsAnalysis(
        salaryConfig: SalaryConfig,
        allocations: List<BudgetAllocation>,
        categoryExpenses: Map<String, Double>,
        goalMonthlyContributions: Map<Long, Double>
    ): SavingsAnalysis {
        val salary = salaryConfig.monthlySalary
        val totalExpenses = categoryExpenses.values.sum()

        val maxSavingsLimit = salary * (salaryConfig.savingsPercent / 100.0)
        val actualSavings = maxOf(0.0, salary - totalExpenses)
        val savingsPotentialPercent = if (salary > 0) (actualSavings / salary) * 100 else 0.0
        val overspendAmount = maxOf(0.0, totalExpenses - salary)

        val suggestedReductions = calculateReductionSuggestions(salary, allocations, categoryExpenses)

        val goalEstimates = goalMonthlyContributions.map { (goalId, monthlyNeeded) ->
            val monthsNeeded = if (monthlyNeeded > 0) {
                val maxMonthly = actualSavings.coerceAtLeast(monthlyNeeded)
                (maxMonthly / monthlyNeeded).toInt().coerceAtLeast(1)
            } else 999
            goalId to "$monthsNeeded months"
        }.toMap()

        return SavingsAnalysis(
            monthlySalary = salary,
            totalExpenses = totalExpenses,
            actualSavings = actualSavings,
            maxSavingsPotential = maxSavingsLimit,
            savingsPotentialPercent = savingsPotentialPercent,
            overspendAmount = overspendAmount,
            suggestedReductions = suggestedReductions,
            goalCompletionEstimates = goalEstimates
        )
    }

    private fun calculateReductionSuggestions(
        salary: Double,
        allocations: List<BudgetAllocation>,
        categoryExpenses: Map<String, Double>
    ): List<SuggestedReduction> {
        val suggestions = mutableListOf<SuggestedReduction>()

        allocations.forEach { alloc ->
            val recommended = salary * (alloc.allocatedPercent / 100.0)
            val current = categoryExpenses[alloc.category] ?: 0.0
            if (current > recommended && recommended > 0) {
                suggestions.add(
                    SuggestedReduction(
                        category = alloc.category,
                        currentSpending = current,
                        recommendedLimit = recommended,
                        potentialSaving = current - recommended
                    )
                )
            }
        }

        return suggestions.sortedByDescending { it.potentialSaving }
    }

    fun getCategorySpendingSummary(
        expenses: List<com.financeapp.data.Expense>
    ): Map<String, Double> {
        return expenses.groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }
}
