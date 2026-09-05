package com.marineus.lastmarketbender.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.marineus.lastmarketbender.data.local.AppDatabase
import com.marineus.lastmarketbender.data.model.BusinessType
import com.marineus.lastmarketbender.data.model.MarketPin
import com.marineus.lastmarketbender.data.repository.PinRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PinRepository
    val pins: StateFlow<List<MarketPin>>

    var locationPermissionGranted by mutableStateOf(false)
        private set

    var userLocation by mutableStateOf<LatLng?>(null)
        private set

    private val sharedPrefs = application.getSharedPreferences("map_prefs", Context.MODE_PRIVATE)
    var isDarkMode by mutableStateOf(sharedPrefs.getBoolean("is_dark_mode", false))
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
        sharedPrefs.edit().putBoolean("is_dark_mode", isDarkMode).apply()
    }

    fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val context = getApplication<Application>().applicationContext
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "pin_image_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun addPin(latLng: LatLng) {
        viewModelScope.launch {
            repository.insert(
                MarketPin(
                    name = "Yeni İşletme",
                    type = BusinessType.MARKET,
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

    fun deletePinById(id: Int) {
        viewModelScope.launch {
            pins.value.find { it.id == id }?.let { pin ->
                // Delete associated images from storage
                pin.imagePaths.forEach { path ->
                    val file = File(path)
                    if (file.exists()) file.delete()
                }
                repository.delete(pin)
            }
        }
    }
}
