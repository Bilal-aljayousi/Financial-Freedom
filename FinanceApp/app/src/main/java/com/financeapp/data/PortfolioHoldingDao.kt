package com.financeapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioHoldingDao {
    @Query("SELECT * FROM portfolio_holdings ORDER BY symbol ASC")
    fun getAllHoldings(): Flow<List<PortfolioHolding>>

    @Query("SELECT * FROM portfolio_holdings WHERE id = :id")
    fun getHoldingById(id: Long): Flow<PortfolioHolding?>

    @Insert
    suspend fun insert(holding: PortfolioHolding): Long

    @Update
    suspend fun update(holding: PortfolioHolding)

    @Delete
    suspend fun delete(holding: PortfolioHolding)

    @Query("SELECT SUM(quantity * purchasePrice) FROM portfolio_holdings")
    fun getTotalInvested(): Flow<Double?>

    @Query("SELECT SUM(quantity * currentPrice) FROM portfolio_holdings")
    fun getCurrentValue(): Flow<Double?>

    @Query("SELECT * FROM portfolio_holdings ORDER BY symbol ASC")
    suspend fun getAllHoldingsList(): List<PortfolioHolding>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAll(holdings: List<PortfolioHolding>)

    @Query("DELETE FROM portfolio_holdings")
    suspend fun deleteAll()
}
