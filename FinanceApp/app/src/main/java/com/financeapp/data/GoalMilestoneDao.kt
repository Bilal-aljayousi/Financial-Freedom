package com.financeapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalMilestoneDao {
    @Query("SELECT * FROM goal_milestones WHERE goalId = :goalId ORDER BY orderIndex ASC, deadline ASC")
    fun getMilestonesByGoalId(goalId: Long): Flow<List<GoalMilestone>>

    @Query("SELECT * FROM goal_milestones WHERE goalId = :goalId ORDER BY orderIndex ASC, deadline ASC")
    suspend fun getMilestonesByGoalIdSync(goalId: Long): List<GoalMilestone>

    @Query("SELECT * FROM goal_milestones ORDER BY deadline ASC")
    fun getAllMilestones(): Flow<List<GoalMilestone>>

    @Query("SELECT * FROM goal_milestones WHERE id = :id")
    fun getMilestoneById(id: Long): Flow<GoalMilestone?>

    @Query("SELECT * FROM goal_milestones WHERE id = :id")
    suspend fun getMilestoneByIdSync(id: Long): GoalMilestone?

    @Query("SELECT SUM(targetAmount) FROM goal_milestones WHERE goalId = :goalId")
    fun getTotalTargetForGoal(goalId: Long): Flow<Double?>

    @Query("SELECT SUM(currentAmount) FROM goal_milestones WHERE goalId = :goalId")
    fun getTotalCurrentForGoal(goalId: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM goal_milestones WHERE goalId = :goalId AND isCompleted = 1")
    fun getCompletedCountForGoal(goalId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM goal_milestones WHERE goalId = :goalId")
    fun getTotalCountForGoal(goalId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(milestone: GoalMilestone): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(milestones: List<GoalMilestone>)

    @Update
    suspend fun update(milestone: GoalMilestone)

    @Delete
    suspend fun delete(milestone: GoalMilestone)

    @Query("DELETE FROM goal_milestones WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM goal_milestones WHERE goalId = :goalId")
    suspend fun deleteAllForGoal(goalId: Long)

    @Query("DELETE FROM goal_milestones")
    suspend fun deleteAll()

    @Query("UPDATE goal_milestones SET currentAmount = :amount WHERE id = :id")
    suspend fun updateAmount(id: Long, amount: Double)

    @Query("UPDATE goal_milestones SET isCompleted = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun updateCompleted(id: Long, completed: Boolean, completedAt: Long?)
}
