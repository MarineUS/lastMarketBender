package com.marineus.lastmarketbender.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.marineus.lastmarketbender.data.local.AppDatabase
import com.marineus.lastmarketbender.data.model.MarketPin
import com.marineus.lastmarketbender.data.repository.PinRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PinRepository
    val pins: StateFlow<List<MarketPin>>

    var locationPermissionGranted by mutableStateOf(false)
        private set

    var userLocation by mutableStateOf<LatLng?>(null)
        private set

    var isDarkMode by mutableStateOf(false)
        private set

    init {
        val pinDao = AppDatabase.getDatabase(application).pinDao()
        repository = PinRepository(pinDao)
        pins = repository.allPins.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun updatePermissionStatus(granted: Boolean) {
        locationPermissionGranted = granted
    }

    fun updateUserLocation(location: LatLng) {
        userLocation = location
    }

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }

    fun addPin(latLng: LatLng) {
        viewModelScope.launch {
            repository.insert(
                MarketPin(
                    name = "Yeni Market",
                    rating = 0f,
                    description = "",
                    latitude = latLng.latitude,
                    longitude = latLng.longitude
                )
            )
        }
    }

    fun updatePin(pin: MarketPin) {
        viewModelScope.launch {
            repository.update(pin)
        }
    }
    
    fun deletePin(pin: MarketPin) {
        viewModelScope.launch {
            repository.delete(pin)
        }
    }

    fun deletePinById(id: Int) {
        viewModelScope.launch {
            // Mevcut pins listesinden ID'ye göre bulup sil
            pins.value.find { it.id == id }?.let { pin ->
                repository.delete(pin)
            }
        }
    }
}
