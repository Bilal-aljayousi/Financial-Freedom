package com.financeapp.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val database: FinanceDatabase) {
    private val expenseDao = database.expenseDao()
    private val savingsGoalDao = database.savingsGoalDao()
    private val portfolioHoldingDao = database.portfolioHoldingDao()
    private val salaryConfigDao = database.salaryConfigDao()
    private val budgetAllocationDao = database.budgetAllocationDao()
    private val goalMilestoneDao = database.goalMilestoneDao()

    // Expense operations
    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getExpensesByMonth(month: Int, year: Int): Flow<List<Expense>> =
        expenseDao.getExpensesByMonth(month, year)

    fun getMonthlyTotal(month: Int, year: Int): Flow<Double?> =
        expenseDao.getMonthlyTotal(month, year)

    fun getCategoryTotals(month: Int, year: Int): Flow<List<CategoryTotal>> =
        expenseDao.getCategoryTotals(month, year)

    fun getDailyTotal(dayStart: Long, dayEnd: Long): Flow<Double?> =
        expenseDao.getDailyTotal(dayStart, dayEnd)

    fun getWeeklyTotal(weekStart: Long, weekEnd: Long): Flow<Double?> =
        expenseDao.getWeeklyTotal(weekStart, weekEnd)

    fun getExpensesInRange(startDate: Long, endDate: Long): Flow<List<Expense>> =
        expenseDao.getExpensesInRange(startDate, endDate)

    fun getCategoryTotalsInRange(startDate: Long, endDate: Long): Flow<List<CategoryTotal>> =
        expenseDao.getCategoryTotalsInRange(startDate, endDate)

    fun getCategoryTotalInRange(startDate: Long, endDate: Long, category: String): Flow<Double?> =
        expenseDao.getCategoryTotalInRange(startDate, endDate, category)

    suspend fun insertExpense(expense: Expense) = expenseDao.insert(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)

    // Savings operations
    fun getAllSavingsGoals(): Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()

    fun getSavingsGoalById(id: Long): Flow<SavingsGoal?> = savingsGoalDao.getGoalById(id)

    suspend fun insertSavingsGoal(goal: SavingsGoal) = savingsGoalDao.insert(goal)

    suspend fun updateSavingsGoal(goal: SavingsGoal) = savingsGoalDao.update(goal)

    suspend fun deleteSavingsGoal(goal: SavingsGoal) = savingsGoalDao.delete(goal)

    suspend fun updateSavingsAmount(id: Long, amount: Double) =
        savingsGoalDao.updateAmount(id, amount)

    // Portfolio operations
    fun getAllHoldings(): Flow<List<PortfolioHolding>> = portfolioHoldingDao.getAllHoldings()

    fun getHoldingById(id: Long): Flow<PortfolioHolding?> = portfolioHoldingDao.getHoldingById(id)

    fun getTotalInvested(): Flow<Double?> = portfolioHoldingDao.getTotalInvested()

    fun getCurrentValue(): Flow<Double?> = portfolioHoldingDao.getCurrentValue()

    suspend fun insertHolding(holding: PortfolioHolding) = portfolioHoldingDao.insert(holding)

    suspend fun updateHolding(holding: PortfolioHolding) = portfolioHoldingDao.update(holding)

    suspend fun deleteHolding(holding: PortfolioHolding) = portfolioHoldingDao.delete(holding)

    // Salary Config operations
    fun getSalaryConfig(): Flow<SalaryConfig?> = salaryConfigDao.getSalaryConfig()

    suspend fun updateSalaryConfig(config: SalaryConfig) = salaryConfigDao.insert(config)

    // Budget Allocation operations
    fun getAllAllocations(): Flow<List<BudgetAllocation>> = budgetAllocationDao.getAllAllocations()

    fun getAllocationsByGroup(group: String): Flow<List<BudgetAllocation>> =
        budgetAllocationDao.getAllocationsByGroup(group)

    suspend fun updateAllocations(allocations: List<BudgetAllocation>) {
        budgetAllocationDao.deleteAll()
        budgetAllocationDao.insertAll(allocations)
    }

    suspend fun updateAllocation(allocation: BudgetAllocation) = budgetAllocationDao.update(allocation)

    // ===== GOAL MILESTONE OPERATIONS =====

    fun getMilestonesByGoalId(goalId: Long): Flow<List<GoalMilestone>> =
        goalMilestoneDao.getMilestonesByGoalId(goalId)

    fun getAllMilestones(): Flow<List<GoalMilestone>> = goalMilestoneDao.getAllMilestones()

    fun getMilestoneById(id: Long): Flow<GoalMilestone?> = goalMilestoneDao.getMilestoneById(id)

    suspend fun getMilestoneByIdSync(id: Long): GoalMilestone? = goalMilestoneDao.getMilestoneByIdSync(id)

    fun getTotalTargetForGoal(goalId: Long): Flow<Double?> = goalMilestoneDao.getTotalTargetForGoal(goalId)

    fun getTotalCurrentForGoal(goalId: Long): Flow<Double?> = goalMilestoneDao.getTotalCurrentForGoal(goalId)

    fun getCompletedCountForGoal(goalId: Long): Flow<Int> = goalMilestoneDao.getCompletedCountForGoal(goalId)

    fun getTotalCountForGoal(goalId: Long): Flow<Int> = goalMilestoneDao.getTotalCountForGoal(goalId)

    suspend fun insertMilestone(milestone: GoalMilestone) = goalMilestoneDao.insert(milestone)

    suspend fun insertMilestones(milestones: List<GoalMilestone>) = goalMilestoneDao.insertAll(milestones)

    suspend fun updateMilestone(milestone: GoalMilestone) = goalMilestoneDao.update(milestone)

    suspend fun deleteMilestone(milestone: GoalMilestone) = goalMilestoneDao.delete(milestone)

    suspend fun deleteMilestoneById(id: Long) = goalMilestoneDao.deleteById(id)

    suspend fun deleteAllMilestonesForGoal(goalId: Long) = goalMilestoneDao.deleteAllForGoal(goalId)

    suspend fun updateMilestoneAmount(id: Long, amount: Double) = goalMilestoneDao.updateAmount(id, amount)

    suspend fun updateMilestoneCompleted(id: Long, completed: Boolean, completedAt: Long?) =
        goalMilestoneDao.updateCompleted(id, completed, completedAt)

    suspend fun getMilestonesForGoalSync(goalId: Long): List<GoalMilestone> =
        goalMilestoneDao.getMilestonesByGoalIdSync(goalId)

    // ===== BACKUP OPERATIONS =====

    suspend fun createBackupData(): BackupData {
        return BackupData(
            expenses = expenseDao.getAllExpensesList(),
            savingsGoals = savingsGoalDao.getAllGoalsList(),
            portfolioHoldings = portfolioHoldingDao.getAllHoldingsList(),
            salaryConfig = salaryConfigDao.getSalaryConfigSync(),
            budgetAllocations = budgetAllocationDao.getAllAllocationsList()
        )
    }

    suspend fun restoreFromBackup(backupData: BackupData, overwrite: Boolean) {
        if (overwrite) {
            expenseDao.deleteAll()
            savingsGoalDao.deleteAll()
            portfolioHoldingDao.deleteAll()
            budgetAllocationDao.deleteAll()
        }

        if (backupData.expenses.isNotEmpty()) {
            expenseDao.insertAll(backupData.expenses)
        }
        if (backupData.savingsGoals.isNotEmpty()) {
            savingsGoalDao.insertAll(backupData.savingsGoals)
        }
        if (backupData.portfolioHoldings.isNotEmpty()) {
            portfolioHoldingDao.insertAll(backupData.portfolioHoldings)
        }
        backupData.salaryConfig?.let {
            salaryConfigDao.insert(it)
        }
        if (backupData.budgetAllocations.isNotEmpty()) {
            budgetAllocationDao.insertAll(backupData.budgetAllocations)
        }
    }

    suspend fun clearAllData() {
        expenseDao.deleteAll()
        savingsGoalDao.deleteAll()
        portfolioHoldingDao.deleteAll()
        budgetAllocationDao.deleteAll()
        goalMilestoneDao.deleteAll()
    }
}
