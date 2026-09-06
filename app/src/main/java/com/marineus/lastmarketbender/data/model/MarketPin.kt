package com.marineus.lastmarketbender.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BusinessType {
    MARKET, KAFE, RESTORAN, MAGAZA
}

@Entity(tableName = "market_pins")
data class MarketPin(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: BusinessType = BusinessType.MARKET,
    val rating: Float,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imagePaths: List<String> = emptyList(),
    
    // Type-specific fields
    val priceLevel: Int = 0, // 1: Ucuz, 2: Orta, 3: Pahalı
    val hasWiFi: Boolean = false,
    val cuisineType: String = "",
    val productCategory: String = "" // Giyim, Teknoloji vb.
)
