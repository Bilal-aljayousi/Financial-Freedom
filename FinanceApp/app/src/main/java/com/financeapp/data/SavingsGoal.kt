package com.financeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: Long,
    val createdAt: Long = System.currentTimeMillis()
) {
    val monthlyNeeded: Double
        get() {
            val monthsLeft = monthsRemaining
            return if (monthsLeft > 0) remainingAmount / monthsLeft else remainingAmount
        }

    val dailyNeeded: Double
        get() {
            val daysLeft = daysRemaining
            return if (daysLeft > 0) remainingAmount / daysLeft else remainingAmount
        }

    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)

    val progress: Float
        get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    val monthsRemaining: Int
        get() {
            val days = daysRemaining
            return (days / 30).toInt().coerceAtLeast(1)
        }

    val daysRemaining: Long
        get() {
            val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(deadline - System.currentTimeMillis())
            return days.coerceAtLeast(0)
        }

    val isCompleted: Boolean
        get() = currentAmount >= targetAmount

    val isOverdue: Boolean
        get() = !isCompleted && System.currentTimeMillis() > deadline
}
