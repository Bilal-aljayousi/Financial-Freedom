package com.financeapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financeapp.MainActivity
import java.util.Calendar

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Only send reminder between 6pm-10pm (evening reminder)
        if (hour in 18..22) {
            showReminderNotification()
        }

        return Result.success()
    }

    private fun showReminderNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "expenses")
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            300,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val tips = listOf(
            "Don't forget to log your expenses today!",
            "Track every purchase to stay on budget",
            "Small expenses add up - log them now",
            "Your budget thanks you for tracking!",
            "Log expenses while you remember them"
        )
        val tip = tips.random()

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID_EXPENSES)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("Daily Expense Reminder")
            .setContentText(tip)
            .setStyle(NotificationCompat.BigTextStyle().bigText(tip))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(DAILY_REMINDER_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted
        }
    }

    companion object {
        const val WORK_NAME = "daily_expense_reminder"
        const val DAILY_REMINDER_NOTIFICATION_ID = 300
    }
}
