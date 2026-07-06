package com.financeapp.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    fun format(amount: Double): String = currencyFormat.format(amount)

    fun formatCompact(amount: Double): String {
        return when {
            amount >= 1_000_000 -> "$${String.format("%.1f", amount / 1_000_000)}M"
            amount >= 1_000 -> "$${String.format("%.1f", amount / 1_000)}K"
            else -> format(amount)
        }
    }

    fun formatPercentage(value: Double): String {
        val sign = if (value >= 0) "+" else ""
        return "$sign${String.format("%.2f", value)}%"
    }
}
