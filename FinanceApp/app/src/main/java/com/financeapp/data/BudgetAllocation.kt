package com.financeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_allocations")
data class BudgetAllocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val allocatedPercent: Double,
    val group: String,
    val isCustom: Boolean = false
)
