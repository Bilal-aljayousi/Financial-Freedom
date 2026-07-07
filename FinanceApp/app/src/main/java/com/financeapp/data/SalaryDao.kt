package com.financeapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryConfigDao {
    @Query("SELECT * FROM salary_config WHERE id = 1")
    fun getSalaryConfig(): Flow<SalaryConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: SalaryConfig)

    @Update
    suspend fun update(config: SalaryConfig)

    @Query("SELECT * FROM salary_config WHERE id = 1")
    suspend fun getSalaryConfigSync(): SalaryConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<SalaryConfig>)
}

@Dao
interface BudgetAllocationDao {
    @Query("SELECT * FROM budget_allocations ORDER BY `group` ASC, category ASC")
    fun getAllAllocations(): Flow<List<BudgetAllocation>>

    @Query("SELECT * FROM budget_allocations WHERE `group` = :group ORDER BY category ASC")
    fun getAllocationsByGroup(group: String): Flow<List<BudgetAllocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(allocations: List<BudgetAllocation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(allocation: BudgetAllocation)

    @Update
    suspend fun update(allocation: BudgetAllocation)

    @Query("DELETE FROM budget_allocations")
    suspend fun deleteAll()

    @Query("SELECT * FROM budget_allocations ORDER BY `group` ASC, category ASC")
    suspend fun getAllAllocationsList(): List<BudgetAllocation>
}
