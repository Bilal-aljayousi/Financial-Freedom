package com.financeapp.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val database: FinanceDatabase) {
    private val salaryConfigDao = database.salaryConfigDao()
    private val savingsGoalDao = database.savingsGoalDao()

    // Salary
    fun getSalaryConfig(): Flow<SalaryConfig?> = salaryConfigDao.getSalaryConfig()
    suspend fun updateSalaryConfig(config: SalaryConfig) = salaryConfigDao.insert(config)
    suspend fun getSalaryConfigSync(): SalaryConfig? = salaryConfigDao.getSalaryConfigSync()

    // Goals
    fun getAllGoals(): Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()
    fun getGoalById(id: Long): Flow<SavingsGoal?> = savingsGoalDao.getGoalById(id)
    suspend fun getGoalByIdSync(id: Long): SavingsGoal? = savingsGoalDao.getGoalByIdSync(id)
    suspend fun insertGoal(goal: SavingsGoal) = savingsGoalDao.insert(goal)
    suspend fun updateGoal(goal: SavingsGoal) = savingsGoalDao.update(goal)
    suspend fun deleteGoal(goal: SavingsGoal) = savingsGoalDao.delete(goal)
    suspend fun updateGoalAmount(id: Long, amount: Double) = savingsGoalDao.updateAmount(id, amount)
}
