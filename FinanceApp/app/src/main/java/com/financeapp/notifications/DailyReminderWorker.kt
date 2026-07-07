package com.financeapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financeapp.FinanceApp
import com.financeapp.MainActivity
import com.financeapp.data.FinanceDatabase
import com.financeapp.data.FinanceRepository
import java.util.Calendar

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Only send reminder between 8am-9pm
        if (hour in 8..21) {
            showReminderNotification()
        }

        return Result.success()
    }

    private suspend fun showReminderNotification() {
        val db = FinanceDatabase.getDatabase(applicationContext)
        val repository = FinanceRepository(db)

        val goals = repository.getAllGoals().let { flow ->
            // Get first non-completed goal
            val allGoals = mutableListOf<com.financeapp.data.SavingsGoal>()
            flow.collect { allGoals.addAll(it) }
            allGoals
        }

        val activeGoal = goals.firstOrNull { !it.isCompleted } ?: return
        val dailyNeeded = activeGoal.dailyNeeded
        val currency = repository.getSalaryConfigSync()?.currency ?: "JOD"

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            300,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messages = listOf(
            "Save ${formatAmount(dailyNeeded, currency)} today for '${activeGoal.name}'",
            "Don't forget! ${formatAmount(dailyNeeded, currency)} today keeps you on track for '${activeGoal.name}'",
            "Your goal '${activeGoal.name}' needs ${formatAmount(dailyNeeded, currency)} today",
            "Stay consistent! Save ${formatAmount(dailyNeeded, currency)} for '${activeGoal.name}'"
        )
        val message = messages.random()

        val notification = NotificationCompat.Builder(applicationContext, FinanceApp.DAILY_REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Daily Savings Reminder")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(DAILY_REMINDER_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted
        }
    }

    private fun formatAmount(amount: Double, currency: String): String {
        return when (currency) {
            "JOD" -> "${String.format("%.3f", amount)} JOD"
            "USD" -> "$${String.format("%.2f", amount)}"
            "EUR" -> "\u20AC${String.format("%.2f", amount)}"
            "SAR" -> "${String.format("%.2f", amount)} SAR"
            else -> "${String.format("%.2f", amount)} $currency"
        }
    }

    companion object {
        const val WORK_NAME = "daily_savings_reminder"
        const val DAILY_REMINDER_NOTIFICATION_ID = 300
    }
}
