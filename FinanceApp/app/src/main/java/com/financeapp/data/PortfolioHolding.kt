package com.financeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_holdings")
data class PortfolioHolding(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val name: String,
    val quantity: Double,
    val purchasePrice: Double,
    val currentPrice: Double = 0.0,
    val purchaseDate: Long = System.currentTimeMillis()
)
