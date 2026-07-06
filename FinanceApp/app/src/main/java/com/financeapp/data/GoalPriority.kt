package com.financeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_priorities")
data class GoalPriority(
    @PrimaryKey val goalId: Long,
    val priority: Int = 2,
    val notes: String = "",
    val monthlyContribution: Double = 0.0
)

enum class Priority(val level: Int, val label: String) {
    HIGH(1, "High"),
    MEDIUM(2, "Medium"),
    LOW(3, "Low");

    companion object {
        fun fromLevel(level: Int): Priority = when (level) {
            1 -> HIGH
            2 -> MEDIUM
            else -> LOW
        }
    }
}
