package com.financeapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goal_milestones",
    foreignKeys = [
        ForeignKey(
            entity = SavingsGoal::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("goalId")]
)
data class GoalMilestone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val title: String,
    val description: String = "",
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: Long,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0
) {
    val progress: Float
        get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)

    val daysRemaining: Long
        get() {
            val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(deadline - System.currentTimeMillis())
            return days.coerceAtLeast(0)
        }

    val isOverdue: Boolean
        get() = !isCompleted && System.currentTimeMillis() > deadline

    val monthlyNeeded: Double
        get() {
            val days = daysRemaining
            return if (days > 0) remainingAmount / (days / 30.0) else remainingAmount
        }
}
