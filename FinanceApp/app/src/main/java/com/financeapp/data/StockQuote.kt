package com.financeapp.data

data class StockQuote(
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val previousClose: Double = 0.0
)
