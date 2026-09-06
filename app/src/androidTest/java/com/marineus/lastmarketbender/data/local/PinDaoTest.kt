package com.marineus.lastmarketbender.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marineus.lastmarketbender.data.model.BusinessType
import com.marineus.lastmarketbender.data.model.MarketPin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PinDaoTest {

    private lateinit var pinDao: PinDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        pinDao = db.pinDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writePinAndReadInList() = runBlocking {
        val pin = MarketPin(
            name = "Test Market",
            type = BusinessType.MARKET,
            rating = 4.5f,
            description = "Good",
            latitude = 1.0,
            longitude = 1.0
        )
        pinDao.insertPin(pin)
        
        val allPins = pinDao.getAllPins().first()
        assertEquals(allPins[0].name, "Test Market")
    }

    @Test
    fun updatePinAndCheckValues() = runBlocking {
        val pin = MarketPin(
            id = 1,
            name = "Old Name",
            type = BusinessType.MARKET,
            rating = 1f,
            description = "",
            latitude = 0.0,
            longitude = 0.0
        )
        pinDao.insertPin(pin)

        val updatedPin = pin.copy(name = "New Name", rating = 5f)
        pinDao.updatePin(updatedPin)

        val allPins = pinDao.getAllPins().first()
        assertEquals("New Name", allPins[0].name)
        assertEquals(5f, allPins[0].rating)
    }

    @Test
    fun deletePinAndCheckEmpty() = runBlocking {
        val pin = MarketPin(
            id = 1,
            name = "Delete Me",
            type = BusinessType.MARKET,
            rating = 0f,
            description = "",
            latitude = 0.0,
            longitude = 0.0
        )
        pinDao.insertPin(pin)
        pinDao.deletePin(pin)

        val allPins = pinDao.getAllPins().first()
        assertEquals(true, allPins.isEmpty())
    }
}
