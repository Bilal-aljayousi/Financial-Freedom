package com.financeapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Callback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Expense::class,
        SavingsGoal::class,
        PortfolioHolding::class,
        SalaryConfig::class,
        BudgetAllocation::class,
        GoalMilestone::class
    ],
    version = 3,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun portfolioHoldingDao(): PortfolioHoldingDao
    abstract fun salaryConfigDao(): SalaryConfigDao
    abstract fun budgetAllocationDao(): BudgetAllocationDao
    abstract fun goalMilestoneDao(): GoalMilestoneDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                    .addCallback(SeedingCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class SeedingCallback(private val appContext: Context) : Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(appContext)
                    val allocationDao = database.budgetAllocationDao()
                    allocationDao.insertAll(defaultAllocations())
                }
            }

            fun defaultAllocations(): List<BudgetAllocation> = listOf(
                BudgetAllocation(category = "Housing", allocatedPercent = 30.0, group = "Needs"),
                BudgetAllocation(category = "Groceries", allocatedPercent = 10.0, group = "Needs"),
                BudgetAllocation(category = "Utilities", allocatedPercent = 5.0, group = "Needs"),
                BudgetAllocation(category = "Transport", allocatedPercent = 5.0, group = "Needs"),
                BudgetAllocation(category = "Insurance", allocatedPercent = 3.0, group = "Needs"),
                BudgetAllocation(category = "Healthcare", allocatedPercent = 2.0, group = "Needs"),
                BudgetAllocation(category = "Dining Out", allocatedPercent = 10.0, group = "Wants"),
                BudgetAllocation(category = "Entertainment", allocatedPercent = 8.0, group = "Wants"),
                BudgetAllocation(category = "Shopping", allocatedPercent = 7.0, group = "Wants"),
                BudgetAllocation(category = "Subscriptions", allocatedPercent = 5.0, group = "Wants"),
                BudgetAllocation(category = "Emergency Fund", allocatedPercent = 10.0, group = "Savings"),
                BudgetAllocation(category = "Retirement", allocatedPercent = 7.0, group = "Savings"),
                BudgetAllocation(category = "Investments", allocatedPercent = 3.0, group = "Savings")
            )
        }
    }
}
