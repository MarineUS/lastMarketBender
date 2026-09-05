package com.marineus.lastmarketbender.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.marineus.lastmarketbender.data.model.BusinessType
import com.marineus.lastmarketbender.data.model.MarketPin
import com.marineus.lastmarketbender.ui.components.RatingBar
import com.marineus.lastmarketbender.ui.components.SectionLabel
import com.marineus.lastmarketbender.ui.viewmodels.MapViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(viewModel: MapViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val pins by viewModel.pins.collectAsState()
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    val sheetState = rememberModalBottomSheetState()
    val listSheetState = rememberModalBottomSheetState()
    val statsSheetState = rememberModalBottomSheetState()
    
    var showSheet by remember { mutableStateOf(false) }
    var showListSheet by remember { mutableStateOf(false) }
    var showStatsSheet by remember { mutableStateOf(false) }
    var selectedPinId by remember { mutableStateOf<Int?>(null) }
    
    val selectedPin = remember(selectedPinId, pins) {
        pins.find { it.id == selectedPinId }
    }

    val mapProperties = remember(viewModel.locationPermissionGranted, viewModel.isDarkMode) {
        MapProperties(
            isMyLocationEnabled = viewModel.locationPermissionGranted,
            mapStyleOptions = if (viewModel.isDarkMode) {
                MapStyleOptions(
                    "[\n" +
                            "  {\n" +
                            "    \"elementType\": \"geometry\",\n" +
                            "    \"stylers\": [{\"color\": \"#242f3e\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"elementType\": \"labels.text.stroke\",\n" +
                            "    \"stylers\": [{\"color\": \"#242f3e\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"elementType\": \"labels.text.fill\",\n" +
                            "    \"stylers\": [{\"color\": \"#746855\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"administrative.locality\",\n" +
                            "    \"elementType\": \"labels.text.fill\",\n" +
                            "    \"stylers\": [{\"color\": \"#d59563\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"poi\",\n" +
                            "    \"elementType\": \"labels.text.fill\",\n" +
                            "    \"stylers\": [{\"color\": \"#d59563\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"poi.park\",\n" +
                            "    \"elementType\": \"geometry\",\n" +
                            "    \"stylers\": [{\"color\": \"#263c3f\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"poi.park\",\n" +
                            "    \"elementType\": \"labels.text.fill\",\n" +
                            "    \"stylers\": [{\"color\": \"#6b9a76\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"road\",\n" +
                            "    \"elementType\": \"geometry\",\n" +
                            "    \"stylers\": [{\"color\": \"#38414e\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"road\",\n" +
                            "    \"elementType\": \"geometry.stroke\",\n" +
                            "    \"stylers\": [{\"color\": \"#212a37\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"road\",\n" +
                            "    \"elementType\": \"labels.text.fill\",\n" +
                            "    \"stylers\": [{\"color\": \"#9ca5b3\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"road.highway\",\n" +
                            "    \"elementType\": \"geometry\",\n" +
                            "    \"stylers\": [{\"color\": \"#746855\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"road.highway\",\n" +
                            "    \"elementType\": \"geometry.stroke\",\n" +
                            "    \"stylers\": [{\"color\": \"#1f2835\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"road.highway\",\n" +
                            "    \"elementType\": \"labels.text.fill\",\n" +
                            "    \"stylers\": [{\"color\": \"#f3d19c\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"transit\",\n" +
                            "    \"elementType\": \"geometry\",\n" +
                            "    \"stylers\": [{\"color\": \"#2f3948\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"transit.station\",\n" +
                            "    \"elementType\": \"labels.text.fill\",\n" +
                            "    \"stylers\": [{\"color\": \"#d59563\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"water\",\n" +
                            "    \"elementType\": \"geometry\",\n" +
                            "    \"stylers\": [{\"color\": \"#17263c\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"water\",\n" +
                            "    \"elementType\": \"labels.text.fill\",\n" +
                            "    \"stylers\": [{\"color\": \"#515c6d\"}]\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"featureType\": \"water\",\n" +
                            "    \"elementType\": \"labels.text.stroke\",\n" +
                            "    \"stylers\": [{\"color\": \"#17263c\"}]\n" +
                            "  }\n" +
                            "]"
                )
            } else null
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.updatePermissionStatus(permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true)
    }

    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.updatePermissionStatus(isGranted)
    }

    // Location Tracking Logic
    DisposableEffect(viewModel.locationPermissionGranted) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    viewModel.updateUserLocation(latLng)
                    
                    // Initial camera focus if needed
                    if (cameraPositionState.position.target == LatLng(0.0, 0.0)) {
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                }
            }
        }

        if (viewModel.locationPermissionGranted) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // 5 saniyede bir
                .setMinUpdateIntervalMillis(2000) // En az 2 saniye
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            onMapClick = { clickedLatLng ->
                val userLoc = viewModel.userLocation
                if (userLoc != null) {
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        userLoc.latitude, userLoc.longitude,
                        clickedLatLng.latitude, clickedLatLng.longitude,
                        results
                    )
                    val distanceInMeters = results[0]
                    if (distanceInMeters <= 20) {
                        viewModel.addPin(clickedLatLng)
                    } else {
                        Toast.makeText(context, "Sadece 20 metre yakınınızdaki yerlere pin ekleyebilirsiniz!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Konumunuz henüz belirlenmedi.", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            pins.forEach { pin ->
                key(pin.id, pin.type) {
                    val markerColor = when (pin.type) {
                        BusinessType.MARKET -> Color(0xFFFF9800) // Turuncu
                        BusinessType.KAFE -> Color(0xFFFFC107)   // Sarı/Altın
                        BusinessType.RESTORAN -> Color(0xFFF44336) // Kırmızı
                        BusinessType.MAGAZA -> Color(0xFF2196F3)  // Mavi
                    }
                    
                    val markerIcon = when (pin.type) {
                        BusinessType.MARKET -> Icons.Default.Store
                        BusinessType.KAFE -> Icons.Default.Coffee
                        BusinessType.RESTORAN -> Icons.Default.Restaurant
                        BusinessType.MAGAZA -> Icons.Default.ShoppingBag
                    }

                    MarkerComposable(
                        state = rememberUpdatedMarkerState(position = LatLng(pin.latitude, pin.longitude)),
                        title = pin.name,
                        onClick = {
                            selectedPinId = pin.id
                            showSheet = true
                            true
                        }
                    ) {
                        // Özel Tasarım Marker
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, shape = CircleShape)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(markerColor, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = markerIcon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showSheet = false
                    selectedPinId = null
                },
                sheetState = sheetState
            ) {
                selectedPin?.let { pin ->
                    PinDetailsEditor(
                        pin = pin,
                        onUpdate = { updatedPin ->
                            viewModel.updatePin(updatedPin)
                        },
                        onDelete = {
                            viewModel.deletePinById(pin.id)
                            showSheet = false
                            selectedPinId = null
                        },
                        onClose = {
                            showSheet = false
                            selectedPinId = null
                        },
                        onSaveImage = { uri ->
                            viewModel.saveImageToInternalStorage(uri)
                        },
                        onCreateTempUri = {
                            viewModel.createTempPictureUri()
                        }
                    )
                }
            }
        }

        if (showListSheet) {
            ModalBottomSheet(
                onDismissRequest = { showListSheet = false },
                sheetState = listSheetState
            ) {
                PinListContent(
                    pins = pins,
                    userLocation = viewModel.userLocation,
                    onPinClick = { pin ->
                        showListSheet = false
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(pin.latitude, pin.longitude), 
                                    17f
                                )
                            )
                            selectedPinId = pin.id
                            showSheet = true
                        }
                    },
                    onClose = { showListSheet = false }
                )
            }
        }

        if (showStatsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showStatsSheet = false },
                sheetState = statsSheetState
            ) {
                DashboardContent(
                    pins = pins,
                    onClose = { showStatsSheet = false }
                )
            }
        }

        // Dashboard Button (Top Right)
        FloatingActionButton(
            onClick = { showStatsSheet = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
                .size(48.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(Icons.Default.Dashboard, contentDescription = "Dashboard")
        }

        // Modern Custom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                onClick = { showListSheet = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Pin Listesi")
            }

            Spacer(modifier = Modifier.height(16.dp))

            FloatingActionButton(
                onClick = {
                    if (viewModel.locationPermissionGranted) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                scope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
                                    )
                                }
                            }
                        }
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }

            Spacer(modifier = Modifier.height(16.dp))

            FloatingActionButton(
                onClick = { viewModel.toggleDarkMode() },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = if (viewModel.isDarkMode) Color.Yellow else Color.Black,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (viewModel.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Dark Mode Toggle"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                FloatingActionButton(
                    onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) } },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color.Gray,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }

                Spacer(modifier = Modifier.height(8.dp))

                FloatingActionButton(
                    onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) } },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color.Gray,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }
            }
        }
    }
}

@Composable
fun PinDetailsEditor(
    pin: MarketPin,
    onUpdate: (MarketPin) -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
    onSaveImage: (Uri) -> String?,
    onCreateTempUri: () -> Uri?
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    
    var name by remember(pin.id, isEditing) { mutableStateOf(pin.name) }
    var type by remember(pin.id, isEditing) { mutableStateOf(pin.type) }
    var description by remember(pin.id, isEditing) { mutableStateOf(pin.description) }
    var rating by remember(pin.id, isEditing) { mutableFloatStateOf(pin.rating) }
    var imagePaths by remember(pin.id, isEditing) { mutableStateOf(pin.imagePaths) }

    // Type-specific states
    var priceLevel by remember(pin.id, isEditing) { mutableIntStateOf(pin.priceLevel) }
    var hasWiFi by remember(pin.id, isEditing) { mutableStateOf(pin.hasWiFi) }
    var cuisineType by remember(pin.id, isEditing) { mutableStateOf(pin.cuisineType) }
    var productCategory by remember(pin.id, isEditing) { mutableStateOf(pin.productCategory) }

    var tempCameraUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var showPhotoOptions by remember { mutableStateOf(false) }
    
    var showFullScreenViewer by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }

    val displayImages = if (isEditing) imagePaths else pin.imagePaths

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                val internalPaths = uris.mapNotNull { onSaveImage(it) }
                imagePaths = imagePaths + internalPaths
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempCameraUriString?.let { uriStr ->
                    val uri = Uri.parse(uriStr)
                    onSaveImage(uri)?.let { path ->
                        imagePaths = imagePaths + path
                    }
                }
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = onCreateTempUri()
                if (uri != null) {
                    tempCameraUriString = uri.toString()
                    cameraLauncher.launch(uri)
                } else {
                    Toast.makeText(context, "Hata: Kamera dosyası hazırlanamadı.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Kamera izni reddedildi.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Düzenle" else "Detaylar",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            if (!isEditing) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat")
                }
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

        // Type Selection Section
        SectionLabel(icon = Icons.Default.Category, label = "İşletme Türü")
        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BusinessType.entries.forEach { businessType ->
                    FilterChip(
                        selected = type == businessType,
                        onClick = { type = businessType },
                        label = { Text(businessType.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            Badge(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = type.name,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name Section
        if (isEditing) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("İşletme Adı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        } else {
            Text(
                text = pin.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Type-Specific Fields
        when (if (isEditing) type else pin.type) {
            BusinessType.MARKET -> {
                SectionLabel(icon = Icons.Default.Payments, label = "Fiyat Seviyesi")
                if (isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..3).forEach { level ->
                            FilterChip(
                                selected = priceLevel == level,
                                onClick = { priceLevel = level },
                                label = { Text("$".repeat(level)) }
                            )
                        }
                    }
                } else {
                    Text("$".repeat(pin.priceLevel.coerceAtLeast(1)), style = MaterialTheme.typography.bodyLarge)
                }
            }
            BusinessType.KAFE -> {
                SectionLabel(icon = Icons.Default.Wifi, label = "İnternet")
                if (isEditing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("WiFi Var mı?")
                        Spacer(Modifier.weight(1f))
                        Switch(checked = hasWiFi, onCheckedChange = { hasWiFi = it })
                    }
                } else {
                    Text(if (pin.hasWiFi) "WiFi Mevcut ✅" else "WiFi Yok ❌")
                }
            }
            BusinessType.RESTORAN -> {
                SectionLabel(icon = Icons.Default.Restaurant, label = "Mutfak Türü")
                if (isEditing) {
                    OutlinedTextField(
                        value = cuisineType,
                        onValueChange = { cuisineType = it },
                        placeholder = { Text("Örn: İtalyan, Kebap...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    Text(pin.cuisineType.ifBlank { "Belirtilmemiş" })
                }
            }
            BusinessType.MAGAZA -> {
                SectionLabel(icon = Icons.Default.ShoppingBag, label = "Kategori")
                if (isEditing) {
                    OutlinedTextField(
                        value = productCategory,
                        onValueChange = { productCategory = it },
                        placeholder = { Text("Örn: Giyim, Kitap...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    Text(pin.productCategory.ifBlank { "Belirtilmemiş" })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rating Section
        SectionLabel(icon = Icons.Default.Star, label = "Puanlama")
        RatingBar(
            rating = if (isEditing) rating else pin.rating,
            onRatingChange = { if (isEditing) rating = it },
            clickable = isEditing,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description Section
        SectionLabel(icon = Icons.Default.Description, label = "Açıklama")
        if (isEditing) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Notlarınız...") },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                minLines = 3,
                shape = MaterialTheme.shapes.medium
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = pin.description.ifBlank { "Açıklama yok." },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp),
                    color = if (pin.description.isNotBlank()) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Photos Section
        SectionLabel(icon = Icons.Default.PhotoLibrary, label = "Fotoğraflar")
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            itemsIndexed(displayImages) { index, imageUri ->
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            selectedImageIndex = index
                            showFullScreenViewer = true
                        }
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (isEditing) {
                        IconButton(
                            onClick = { imagePaths = imagePaths.filter { it != imageUri } },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fotoğrafı Kaldır",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            if (isEditing) {
                item {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .clickable { showPhotoOptions = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Ekle",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Ekle",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        if (showPhotoOptions) {
            AlertDialog(
                onDismissRequest = { showPhotoOptions = false },
                title = { Text("Fotoğraf Ekle") },
                text = { Text("Fotoğrafı nereden eklemek istersiniz?") },
                confirmButton = {
                    TextButton(onClick = {
                        showPhotoOptions = false
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Text("Galeri")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showPhotoOptions = false
                        val hasCameraPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        
                        if (hasCameraPermission) {
                            val uri = onCreateTempUri()
                            if (uri != null) {
                                tempCameraUriString = uri.toString()
                                cameraLauncher.launch(uri)
                            } else {
                                Toast.makeText(context, "Hata: Kamera dosyası hazırlanamadı.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) {
                        Text("Kamera")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons Section
        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { isEditing = false },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("İptal")
                }
                Button(
                    onClick = {
                        onUpdate(pin.copy(
                            name = name,
                            type = type,
                            description = description,
                            rating = rating,
                            imagePaths = imagePaths,
                            priceLevel = priceLevel,
                            hasWiFi = hasWiFi,
                            cuisineType = cuisineType,
                            productCategory = productCategory
                        ))
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Kaydet")
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sil")
                }
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Düzenle")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showFullScreenViewer) {
        FullScreenImageViewer(
            images = displayImages,
            initialIndex = selectedImageIndex,
            onDismiss = { showFullScreenViewer = false }
        )
    }
}

@Composable
fun FullScreenImageViewer(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { images.size }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 16.dp
            ) { page ->
                AsyncImage(
                    model = images[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
            }
            
            // Indicator
            Text(
                text = "${pagerState.currentPage + 1} / ${images.size}",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(Color.Black.copy(alpha = 0.4f), MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun PinListContent(
    pins: List<MarketPin>,
    userLocation: LatLng?,
    onPinClick: (MarketPin) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<BusinessType?>(null) }
    var sortMode by remember { mutableStateOf(SortMode.NONE) }

    val pinDistances = remember(pins, userLocation) {
        pins.associate { pin ->
            val distance = if (userLocation != null) {
                val results = FloatArray(1)
                Location.distanceBetween(
                    userLocation.latitude, userLocation.longitude,
                    pin.latitude, pin.longitude,
                    results
                )
                results[0]
            } else null
            pin.id to distance
        }
    }

    val filteredPins = remember(pins, searchQuery, selectedType, sortMode, pinDistances) {
        pins.filter { pin ->
            val matchesSearch = pin.name.contains(searchQuery, ignoreCase = true)
            val matchesType = selectedType == null || pin.type == selectedType
            matchesSearch && matchesType
        }.let { list ->
            when (sortMode) {
                SortMode.RATING -> list.sortedByDescending { it.rating }
                SortMode.DISTANCE -> list.sortedBy { pinDistances[it.id] ?: Float.MAX_VALUE }
                SortMode.NONE -> list
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kayıtlı Yerler",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Kapat")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("İsimle ara...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Temizle")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filters and Sort Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { selectedType = null },
                        label = { Text("Tümü") }
                    )
                }
                items(BusinessType.entries) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Proximity Sort
            FilterChip(
                selected = sortMode == SortMode.DISTANCE,
                onClick = { 
                    sortMode = if (sortMode == SortMode.DISTANCE) SortMode.NONE else SortMode.DISTANCE 
                },
                label = { Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(18.dp)) },
                leadingIcon = if (sortMode == SortMode.DISTANCE) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Rating Sort
            FilterChip(
                selected = sortMode == SortMode.RATING,
                onClick = { 
                    sortMode = if (sortMode == SortMode.RATING) SortMode.NONE else SortMode.RATING 
                },
                label = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(18.dp)) },
                leadingIcon = if (sortMode == SortMode.RATING) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        if (filteredPins.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (pins.isEmpty()) "Henüz bir yer eklenmemiş." else "Sonuç bulunamadı.",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredPins) { pin ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPinClick(pin) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (pin.type) {
                                    BusinessType.MARKET -> Icons.Default.Store
                                    BusinessType.KAFE -> Icons.Default.Coffee
                                    BusinessType.RESTORAN -> Icons.Default.Restaurant
                                    BusinessType.MAGAZA -> Icons.Default.ShoppingBag
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pin.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pin.type.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    val distance = pinDistances[pin.id]
                                    if (distance != null) {
                                        Text(
                                            text = " • ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = if (distance < 1000) "${distance.toInt()}m" else String.format("%.1fkm", distance / 1000),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", pin.rating),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun DashboardContent(
    pins: List<MarketPin>,
    onClose: () -> Unit
) {
    val totalPins = pins.size
    val marketCount = pins.count { it.type == BusinessType.MARKET }
    val kafeCount = pins.count { it.type == BusinessType.KAFE }
    val restoranCount = pins.count { it.type == BusinessType.RESTORAN }
    val magazaCount = pins.count { it.type == BusinessType.MAGAZA }
    
    val totalPhotos = pins.sumOf { it.imagePaths.size }
    val averageRating = if (pins.isNotEmpty()) pins.map { it.rating }.average() else 0.0
    
    val favoriteType = if (pins.isNotEmpty()) {
        pins.groupBy { it.type }.maxBy { it.value.size }.key.name
    } else "Yok"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kullanım İstatistikleri",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Kapat")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ana Kart: Toplam Pin
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Toplam Kaydedilen Yer", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = totalPins.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Türlere Göre Dağılım
        Text("Kategorilere Göre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatSmallCard(Modifier.weight(1f), "Market", marketCount, Icons.Default.Store, Color(0xFFFF9800))
            StatSmallCard(Modifier.weight(1f), "Kafe", kafeCount, Icons.Default.Coffee, Color(0xFFFFC107))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatSmallCard(Modifier.weight(1f), "Restoran", restoranCount, Icons.Default.Restaurant, Color(0xFFF44336))
            StatSmallCard(Modifier.weight(1f), "Mağaza", magazaCount, Icons.Default.ShoppingBag, Color(0xFF2196F3))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ek Detaylar
        Text("Genel Bakış", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        DetailRow(Icons.Default.PhotoLibrary, "Toplam Fotoğraf", totalPhotos.toString())
        DetailRow(Icons.Default.Star, "Ortalama Puan", String.format("%.1f", averageRating))
        DetailRow(Icons.Default.Favorite, "En Çok Tercih Edilen", favoriteType)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StatSmallCard(modifier: Modifier, label: String, count: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

enum class SortMode {
    NONE, RATING, DISTANCE
}
