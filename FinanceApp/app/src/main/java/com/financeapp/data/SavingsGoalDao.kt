package com.financeapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY deadline ASC")
    fun getAllGoals(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    fun getGoalById(id: Long): Flow<SavingsGoal?>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalByIdSync(id: Long): SavingsGoal?

    @Insert
    suspend fun insert(goal: SavingsGoal): Long

    @Update
    suspend fun update(goal: SavingsGoal)

    @Delete
    suspend fun delete(goal: SavingsGoal)

    @Query("UPDATE savings_goals SET currentAmount = :amount WHERE id = :id")
    suspend fun updateAmount(id: Long, amount: Double)

    @Query("SELECT * FROM savings_goals ORDER BY deadline ASC")
    suspend fun getAllGoalsList(): List<SavingsGoal>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<SavingsGoal>)

    @Query("DELETE FROM savings_goals")
    suspend fun deleteAll()
}
