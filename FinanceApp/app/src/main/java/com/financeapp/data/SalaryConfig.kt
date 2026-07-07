package com.financeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salary_config")
data class SalaryConfig(
    @PrimaryKey val id: Long = 1,
    val monthlySalary: Double = 0.0,
    val currency: String = "JOD",
    val lastUpdated: Long = System.currentTimeMillis()
)
