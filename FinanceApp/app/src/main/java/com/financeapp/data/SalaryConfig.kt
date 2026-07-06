package com.financeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salary_config")
data class SalaryConfig(
    @PrimaryKey val id: Long = 1,
    val monthlySalary: Double = 0.0,
    val needsPercent: Double = 50.0,
    val wantsPercent: Double = 30.0,
    val savingsPercent: Double = 20.0,
    val lastUpdated: Long = System.currentTimeMillis()
)
