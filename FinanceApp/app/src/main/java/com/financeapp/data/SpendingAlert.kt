package com.financeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spending_alerts")
data class SpendingAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val limitAmount: Double,
    val currentSpent: Double,
    val alertType: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

enum class AlertType(val label: String) {
    OVER_BUDGET("Over Budget"),
    APPROACHING_LIMIT("Approaching Limit"),
    CATEGORY_EXCEEDED("Category Exceeded")
}
