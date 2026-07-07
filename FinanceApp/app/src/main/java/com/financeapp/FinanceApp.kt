package com.financeapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.financeapp.notifications.DailyReminderWorker
import java.util.concurrent.TimeUnit

class FinanceApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleNotifications()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val dailyReminderChannel = NotificationChannel(
            DAILY_REMINDER_CHANNEL_ID,
            "Daily Savings Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Daily reminders to save toward your goal"
            enableVibration(true)
        }

        manager.createNotificationChannels(listOf(dailyReminderChannel))
    }

    private fun scheduleNotifications() {
        val workManager = WorkManager.getInstance(this)

        val dailyReminder = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1, TimeUnit.DAYS
        ).setInitialDelay(1, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            DailyReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyReminder
        )
    }

    companion object {
        const val DAILY_REMINDER_CHANNEL_ID = "daily_savings_reminders"
    }
}
