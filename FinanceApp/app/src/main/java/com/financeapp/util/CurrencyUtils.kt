package com.financeapp.util

object CurrencyUtils {
    fun format(amount: Double, currency: String = "JOD"): String {
        return when (currency) {
            "JOD" -> "${String.format("%.3f", amount)} JOD"
            "USD" -> "$${String.format("%.2f", amount)}"
            "EUR" -> "\u20AC${String.format("%.2f", amount)}"
            "SAR" -> "${String.format("%.2f", amount)} SAR"
            else -> "${String.format("%.2f", amount)} $currency"
        }
    }

    fun formatCompact(amount: Double, currency: String = "JOD"): String {
        return when {
            amount >= 1_000_000 -> "${format(amount / 1_000_000, currency)}M"
            amount >= 1_000 -> "${format(amount / 1_000, currency)}K"
            else -> format(amount, currency)
        }
    }
}
