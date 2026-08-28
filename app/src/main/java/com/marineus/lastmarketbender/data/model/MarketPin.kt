package com.marineus.lastmarketbender.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_pins")
data class MarketPin(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val rating: Float,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imagePaths: List<String> = emptyList()
)
