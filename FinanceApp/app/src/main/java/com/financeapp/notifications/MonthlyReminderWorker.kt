package com.financeapp.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import com.financeapp.util.TimeUtils
import kotlinx.coroutines.flow.first

class MonthlyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        NotificationHelper.showMonthlyReminder(applicationContext)

        val db = FinanceDatabase.getDatabase(applicationContext)
        val repository = FinanceRepository(db)

        val goals = repository.getAllSavingsGoals().first()
        val now = System.currentTimeMillis()

        goals.forEach { goal ->
            val daysRemaining = TimeUtils.daysUntil(goal.deadline)
            val remaining = goal.targetAmount - goal.currentAmount

            if (remaining > 0) {
                val monthsRemaining = TimeUtils.monthsUntil(goal.deadline)
                val monthlyNeeded = if (monthsRemaining > 0) remaining / monthsRemaining else remaining

                val monthlyContributions = monthlyNeeded
                val estimatedMonths = if (monthlyContributions > 0) {
                    (remaining / monthlyContributions).toInt()
                } else 999

                if (daysRemaining <= 0 || estimatedMonths > monthsRemaining) {
                    NotificationHelper.showSavingsAlert(
                        context = applicationContext,
                        goalName = goal.name,
                        currentAmount = goal.currentAmount,
                        targetAmount = goal.targetAmount,
                        daysRemaining = daysRemaining
                    )
                }
            }
        }

        return Result.success()
    }
}
