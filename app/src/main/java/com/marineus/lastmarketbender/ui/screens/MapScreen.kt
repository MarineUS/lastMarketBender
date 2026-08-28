package com.marineus.lastmarketbender.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.marineus.lastmarketbender.data.model.MarketPin
import com.marineus.lastmarketbender.ui.viewmodels.MapViewModel
import com.google.android.gms.location.LocationServices
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
    var showSheet by remember { mutableStateOf(false) }
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

    LaunchedEffect(viewModel.locationPermissionGranted) {
        if (viewModel.locationPermissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    viewModel.updateUserLocation(latLng)
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
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
                key(pin.id) {
                    Marker(
                        state = rememberMarkerState(position = LatLng(pin.latitude, pin.longitude)),
                        title = pin.name,
                        snippet = "Puan: ${pin.rating}",
                        onClick = {
                            selectedPinId = pin.id
                            showSheet = true
                            true
                        }
                    )
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
                        }
                    )
                }
            }
        }

        // Modern Custom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
    onClose: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    
    var name by remember(pin.id, isEditing) { mutableStateOf(pin.name) }
    var description by remember(pin.id, isEditing) { mutableStateOf(pin.description) }
    var rating by remember(pin.id, isEditing) { mutableFloatStateOf(pin.rating) }
    var imagePaths by remember(pin.id, isEditing) { mutableStateOf(pin.imagePaths) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                imagePaths = imagePaths + uris.map { it.toString() }
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
                text = if (isEditing) "Market Düzenle" else "Market Detayları",
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

        // Name Section
        if (isEditing) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Market Adı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        } else {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
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
                placeholder = { Text("Fikirlerinizi buraya yazın...") },
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
                    text = if (pin.description.isNotBlank()) pin.description else "Açıklama eklenmemiş.",
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
        
        val displayImages = if (isEditing) imagePaths else pin.imagePaths
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(displayImages) { imageUri ->
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            if (isEditing) {
                item {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .clickable { 
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
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

        if (!isEditing && displayImages.isEmpty()) {
            Text(
                text = "Henüz fotoğraf eklenmemiş.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp)
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
                            description = description,
                            rating = rating,
                            imagePaths = imagePaths
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
}

@Composable
fun SectionLabel(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    stars: Int = 5,
    clickable: Boolean = true
) {
    Row(modifier = modifier) {
        for (i in 1..stars) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray,
                modifier = Modifier
                    .size(32.dp)
                    .then(
                        if (clickable) Modifier.clickable { onRatingChange(i.toFloat()) }
                        else Modifier
                    )
            )
        }
    }
}
