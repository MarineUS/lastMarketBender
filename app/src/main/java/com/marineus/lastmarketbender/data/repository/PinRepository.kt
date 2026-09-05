package com.marineus.lastmarketbender.data.repository

import com.marineus.lastmarketbender.data.local.PinDao
import com.marineus.lastmarketbender.data.model.MarketPin
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PinRepository @Inject constructor(private val pinDao: PinDao) {
    val allPins: Flow<List<MarketPin>> = pinDao.getAllPins()

    suspend fun insert(pin: MarketPin) {
        pinDao.insertPin(pin)
    }

    suspend fun update(pin: MarketPin) {
        pinDao.updatePin(pin)
    }

    suspend fun delete(pin: MarketPin) {
        pinDao.deletePin(pin)
    }
}
