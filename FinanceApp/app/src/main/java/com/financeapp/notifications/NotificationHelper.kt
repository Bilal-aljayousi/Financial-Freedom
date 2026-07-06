package com.financeapp.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.financeapp.FinanceApp
import com.financeapp.MainActivity
import com.financeapp.R

object NotificationHelper {

    fun showBudgetAlert(
        context: Context,
        category: String,
        spent: Double,
        limit: Double,
        percentUsed: Float
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "alerts")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = if (percentUsed > 1.0f) {
            "You've exceeded your $category budget! Spent: $${String.format("%.2f", spent)} / Limit: $${String.format("%.2f", limit)}"
        } else {
            "Warning: You've used ${String.format("%.0f", percentUsed * 100)}% of your $category budget."
        }

        val notification = NotificationCompat.Builder(context, FinanceApp.BUDGET_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Budget Alert: $category")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(
                if (percentUsed > 1.0f) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(category.hashCode(), notification)
    }

    fun showSavingsAlert(
        context: Context,
        goalName: String,
        currentAmount: Double,
        targetAmount: Double,
        daysRemaining: Long
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "goals")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val progress = if (targetAmount > 0) (currentAmount / targetAmount * 100) else 0.0

        val message = if (daysRemaining <= 0) {
            "Your '$goalName' goal deadline has passed! Current: $${String.format("%.2f", currentAmount)} / $${String.format("%.2f", targetAmount)}"
        } else {
            "Your '$goalName' goal is behind schedule. ${String.format("%.1f", progress)}% complete with $daysRemaining days remaining."
        }

        val notification = NotificationCompat.Builder(context, FinanceApp.SAVINGS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Savings Goal Alert")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(goalName.hashCode(), notification)
    }

    fun showMonthlyReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "expenses")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, FinanceApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Monthly Expense Reminder")
            .setContentText("Don't forget to log your expenses for this month!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("It's a new month! Take a moment to log your recent expenses and review your budget. Staying on top of your finances helps you reach your goals faster.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(9999, notification)
    }

    fun showOverBudgetNotification(
        context: Context,
        totalSpent: Double,
        salary: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "alerts")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val overspend = totalSpent - salary
        val message = "You've exceeded your monthly salary by $${String.format("%.2f", overspend)}. Review your spending to get back on track."

        val notification = NotificationCompat.Builder(context, FinanceApp.BUDGET_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Monthly Budget Exceeded!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(8888, notification)
    }
}
