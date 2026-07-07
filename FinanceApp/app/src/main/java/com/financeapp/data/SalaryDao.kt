package com.financeapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryConfigDao {
    @Query("SELECT * FROM salary_config WHERE id = 1")
    fun getSalaryConfig(): Flow<SalaryConfig?>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insert(config: SalaryConfig)

    @Query("SELECT * FROM salary_config WHERE id = 1")
    suspend fun getSalaryConfigSync(): SalaryConfig?
}
