package com.financeapp.data

data class BackupData(
    val version: Int = BACKUP_VERSION,
    val createdAt: Long = System.currentTimeMillis(),
    val deviceName: String = android.os.Build.MODEL,
    val expenses: List<Expense> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val portfolioHoldings: List<PortfolioHolding> = emptyList(),
    val salaryConfig: SalaryConfig? = null,
    val budgetAllocations: List<BudgetAllocation> = emptyList()
) {
    companion object {
        const val BACKUP_VERSION = 1
        const val BACKUP_FILE_PREFIX = "finance_backup_"
        const val BACKUP_FILE_EXTENSION = ".json"
    }
}
