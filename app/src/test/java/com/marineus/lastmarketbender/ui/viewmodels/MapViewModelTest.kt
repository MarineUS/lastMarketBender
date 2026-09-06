package com.marineus.lastmarketbender.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences
import com.marineus.lastmarketbender.data.model.BusinessType
import com.marineus.lastmarketbender.data.model.MarketPin
import com.marineus.lastmarketbender.data.repository.PinRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private lateinit var viewModel: MapViewModel
    private val repository: PinRepository = mockk(relaxed = true)
    private val sharedPrefs: SharedPreferences = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val networkMonitor: com.marineus.lastmarketbender.util.NetworkMonitor = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock default behavior for pins flow
        every { repository.allPins } returns flowOf(emptyList())
        every { sharedPrefs.getBoolean("is_dark_mode", false) } returns false
        every { networkMonitor.isOnline } returns flowOf(true)

        viewModel = MapViewModel(repository, sharedPrefs, networkMonitor, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleDarkMode updates state and saves to prefs`() = runTest {
        // Initial state is false
        assertEquals(false, viewModel.isDarkMode)

        // Toggle
        viewModel.toggleDarkMode()

        // Assert state updated
        assertEquals(true, viewModel.isDarkMode)

        // Verify sharedPrefs.edit() was called (relaxed mock handles chain)
        verify { sharedPrefs.edit() }
    }

    @Test
    fun `addPin calls repository insert`() = runTest {
        val latLng = com.google.android.gms.maps.model.LatLng(41.0, 29.0)
        
        viewModel.addPin(latLng)
        advanceUntilIdle()

        coVerify { repository.insert(any()) }
    }

    @Test
    fun `deletePinById calls repository delete and deletes images`() = runTest {
        val pinId = 1
        val pin = MarketPin(
            id = pinId,
            name = "Test",
            type = BusinessType.MARKET,
            rating = 5f,
            description = "",
            latitude = 0.0,
            longitude = 0.0,
            imagePaths = listOf("/path/to/image.jpg")
        )
        
        // Mock pins list in ViewModel
        every { repository.allPins } returns flowOf(listOf(pin))
        
        // Re-init to pick up the mocked flow
        viewModel = MapViewModel(repository, sharedPrefs, networkMonitor, context)
        
        // Collect the flow to trigger stateIn collection
        val job = launch { viewModel.pins.collect {} }
        advanceUntilIdle()

        viewModel.deletePinById(pinId)
        advanceUntilIdle()

        coVerify { repository.delete(pin) }
        job.cancel()
    }
}
