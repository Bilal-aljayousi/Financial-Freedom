package com.financeapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.financeapp.backup.BackupWorker
import com.financeapp.notifications.BudgetCheckWorker
import com.financeapp.notifications.DailyReminderWorker
import com.financeapp.notifications.MonthlyReminderWorker
import java.util.concurrent.TimeUnit

class FinanceApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleNotifications()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val budgetChannel = NotificationChannel(
            BUDGET_CHANNEL_ID,
            "Budget Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications when spending exceeds budget limits"
            enableVibration(true)
        }

        val reminderChannel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Monthly Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Monthly reminders to log your expenses"
        }

        val savingsChannel = NotificationChannel(
            SAVINGS_CHANNEL_ID,
            "Savings Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when savings goals fall behind schedule"
            enableVibration(true)
        }

        val dailyReminderChannel = NotificationChannel(
            DAILY_REMINDER_CHANNEL_ID,
            "Daily Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily reminders to log your expenses"
        }

        manager.createNotificationChannels(listOf(budgetChannel, reminderChannel, savingsChannel, dailyReminderChannel))
    }

    private fun scheduleNotifications() {
        val workManager = WorkManager.getInstance(this)

        val monthlyReminder = PeriodicWorkRequestBuilder<MonthlyReminderWorker>(
            30, TimeUnit.DAYS
        ).setInitialDelay(1, TimeUnit.DAYS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "monthly_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            monthlyReminder
        )

        val budgetCheck = PeriodicWorkRequestBuilder<BudgetCheckWorker>(
            6, TimeUnit.HOURS
        ).setInitialDelay(1, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "budget_check",
            ExistingPeriodicWorkPolicy.KEEP,
            budgetCheck
        )

        val dailyReminder = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1, TimeUnit.DAYS
        ).setInitialDelay(12, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            DailyReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyReminder
        )

        val autoBackup = PeriodicWorkRequestBuilder<BackupWorker>(
            1, TimeUnit.DAYS
        ).setInitialDelay(6, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            BackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            autoBackup
        )
    }

    companion object {
        const val BUDGET_CHANNEL_ID = "budget_alerts"
        const val REMINDER_CHANNEL_ID = "monthly_reminders"
        const val SAVINGS_CHANNEL_ID = "savings_alerts"
        const val DAILY_REMINDER_CHANNEL_ID = "daily_reminders"
    }
}
