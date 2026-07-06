package com.financeapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE month = :month AND year = :year ORDER BY date DESC")
    fun getExpensesByMonth(month: Int, year: Int): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE month = :month AND year = :year")
    fun getMonthlyTotal(month: Int, year: Int): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE month = :month AND year = :year GROUP BY category")
    fun getCategoryTotals(month: Int, year: Int): Flow<List<CategoryTotal>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :dayStart AND date <= :dayEnd")
    fun getDailyTotal(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :weekStart AND date <= :weekEnd")
    fun getWeeklyTotal(weekStart: Long, weekEnd: Long): Flow<Double?>

    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getExpensesInRange(startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE date >= :startDate AND date <= :endDate GROUP BY category")
    fun getCategoryTotalsInRange(startDate: Long, endDate: Long): Flow<List<CategoryTotal>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startDate AND date <= :endDate AND category = :category")
    fun getCategoryTotalInRange(startDate: Long, endDate: Long, category: String): Flow<Double?>

    @Insert
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpensesList(): List<Expense>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<Expense>)

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
