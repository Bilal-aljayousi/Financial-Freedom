package com.financeapp.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

class BudgetCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = FinanceDatabase.getDatabase(applicationContext)
        val repository = FinanceRepository(db)

        val config = repository.getSalaryConfig().first()
        val salary = config?.monthlySalary ?: return Result.success()

        if (salary <= 0) return Result.success()

        val allocations = repository.getAllAllocations().first()
        val now = Calendar.getInstance()
        val month = now.get(Calendar.MONTH)
        val year = now.get(Calendar.YEAR)

        val expenses = repository.getExpensesByMonth(month, year).first()
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        var totalSpent = 0.0

        allocations.forEach { alloc ->
            val budget = salary * (alloc.allocatedPercent / 100.0)
            val spent = categoryTotals[alloc.category] ?: 0.0
            totalSpent += spent

            val percentUsed = if (budget > 0) (spent / budget).toFloat() else 0f

            if (percentUsed > 0.8f) {
                NotificationHelper.showBudgetAlert(
                    context = applicationContext,
                    category = alloc.category,
                    spent = spent,
                    limit = budget,
                    percentUsed = percentUsed
                )
            }
        }

        if (totalSpent > salary) {
            NotificationHelper.showOverBudgetNotification(
                context = applicationContext,
                totalSpent = totalSpent,
                salary = salary
            )
        }

        return Result.success()
    }
}
