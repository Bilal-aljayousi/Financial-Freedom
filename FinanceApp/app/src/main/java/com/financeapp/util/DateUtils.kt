package com.financeapp.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun formatDate(timestamp: Long): String = displayFormat.format(Date(timestamp))

    fun formatMonth(month: Int, year: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        return monthFormat.format(cal.time)
    }

    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH)

    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun getMonthYearFromDate(timestamp: Long): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return Pair(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
    }
}
