package com.financeapp.util

import java.util.Calendar
import java.util.concurrent.TimeUnit

object TimeUtils {
    fun daysUntil(deadline: Long): Long {
        val now = System.currentTimeMillis()
        val diff = deadline - now
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    fun monthsUntil(deadline: Long): Int {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = deadline }

        var months = (target.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12
        months += target.get(Calendar.MONTH) - now.get(Calendar.MONTH)

        return maxOf(0, months)
    }

    fun getMonthStartEnd(year: Int, month: Int): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val end = Calendar.getInstance().apply {
            set(year, month, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return Pair(start, end)
    }
}
