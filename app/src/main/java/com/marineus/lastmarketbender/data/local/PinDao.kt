package com.marineus.lastmarketbender.data.local

import androidx.room.*
import com.marineus.lastmarketbender.data.model.MarketPin
import kotlinx.coroutines.flow.Flow

@Dao
interface PinDao {
    @Query("SELECT * FROM market_pins")
    fun getAllPins(): Flow<List<MarketPin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPin(pin: MarketPin)

    @Update
    suspend fun updatePin(pin: MarketPin)

    @Delete
    suspend fun deletePin(pin: MarketPin)
}
