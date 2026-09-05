package com.marineus.lastmarketbender.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.FileProvider
import com.google.android.gms.maps.model.LatLng
import com.marineus.lastmarketbender.data.model.BusinessType
import com.marineus.lastmarketbender.data.model.MarketPin
import com.marineus.lastmarketbender.data.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import androidx.core.content.edit

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: PinRepository,
    private val sharedPrefs: SharedPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val pins: StateFlow<List<MarketPin>> = repository.allPins.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    var locationPermissionGranted by mutableStateOf(false)
        private set

    var userLocation by mutableStateOf<LatLng?>(null)
        private set

    var isDarkMode by mutableStateOf(sharedPrefs.getBoolean("is_dark_mode", false))
        private set

    fun updatePermissionStatus(granted: Boolean) {
        locationPermissionGranted = granted
    }

    fun updateUserLocation(location: LatLng) {
        userLocation = location
    }

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
        sharedPrefs.edit { putBoolean("is_dark_mode", isDarkMode) }
    }

    fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
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

    fun createTempPictureUri(): Uri? {
        return try {
            // En garanti yol: External Cache
            val tempFile = File(context.externalCacheDir, "camera_capture.jpg")
            if (tempFile.exists()) tempFile.delete()
            tempFile.createNewFile()
            
            FileProvider.getUriForFile(
                context,
                "com.marineus.lastmarketbender.fileprovider",
                tempFile
            )
        } catch (e: Exception) {
            android.util.Log.e("KAMERA_HATASI", "URI Olusturulamadi: ${e.message}", e)
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
