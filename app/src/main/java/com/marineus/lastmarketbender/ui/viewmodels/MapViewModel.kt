package com.marineus.lastmarketbender.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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
import androidx.core.content.ContextCompat

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

    var isOnline by mutableStateOf(checkInitialNetwork())
        private set

    private fun checkInitialNetwork(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    init {
        observeNetwork()
    }

    private fun observeNetwork() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isOnline = true }
            override fun onLost(network: Network) { isOnline = false }
        })
    }

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

    fun exportDataToJson(): String {
        return Gson().toJson(pins.value)
    }

    fun importDataFromJson(uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.use { it.readText() }
                
                val pinListType = object : TypeToken<List<MarketPin>>() {}.type
                val importedPins: List<MarketPin> = Gson().fromJson(jsonString, pinListType)
                
                importedPins.forEach { pin ->
                    // Reset ID to avoid conflicts and treat as new entries
                    repository.insert(pin.copy(id = 0))
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Bilinmeyen bir hata oluştu")
            }
        }
    }
}
