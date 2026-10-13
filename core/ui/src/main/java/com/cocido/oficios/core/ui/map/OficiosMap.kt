package com.cocido.oficios.core.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosColorAction
import com.cocido.oficios.core.designsystem.theme.OficiosColorSecondaryText
import com.cocido.oficios.core.designsystem.theme.OficiosColorSuccessText
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import kotlinx.coroutines.delay
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private val FormosaCenter = LatLng(-26.1849, -58.1731)

@Composable
fun OficiosLocationPicker(
    latitude: Double?,
    longitude: Double?,
    onLocationSelected: (latitude: Double, longitude: Double) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onPermissionDenied: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLocating: Boolean = false,
) {
    val context = LocalContext.current
    val mapsApiKey = remember { context.googleMapsApiKey() }
    var hasLocationPermission by remember {
        mutableStateOf(context.hasAnyLocationPermission())
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        hasLocationPermission = grants.values.any { it } || context.hasAnyLocationPermission()
        if (hasLocationPermission) onUseCurrentLocation() else onPermissionDenied()
    }
    val selected = latitude?.let { lat -> longitude?.let { lng -> LatLng(lat, lng) } }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selected ?: FormosaCenter, if (selected == null) 12f else 16f)
    }
    var mapLoaded by remember { mutableStateOf(false) }
    var mapUnavailable by remember { mutableStateOf(mapsApiKey.isBlank()) }

    LaunchedEffect(mapsApiKey) {
        if (mapsApiKey.isBlank()) {
            mapUnavailable = true
            return@LaunchedEffect
        }
        delay(MAP_LOAD_TIMEOUT_MILLIS)
        if (!mapLoaded) mapUnavailable = true
    }

    LaunchedEffect(selected) {
        selected?.let { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 16f)) }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
        Text(
            text = "Ubicación exacta",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Mové el mapa o tocá un punto para ubicar la entrada. Podés corregirla manualmente.",
            style = MaterialTheme.typography.bodySmall,
            color = OficiosColorSecondaryText,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .semantics { contentDescription = "Mapa para seleccionar la ubicación" },
        ) {
            if (!mapUnavailable) GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(
                    compassEnabled = true,
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false,
                ),
                onMapClick = { if (enabled) onLocationSelected(it.latitude, it.longitude) },
                onMapLoaded = { mapLoaded = true },
            ) {
                selected?.let {
                    Marker(
                        state = remember(it) { MarkerState(position = it) },
                        title = "Ubicación seleccionada",
                    )
                }
            }
            if (!mapLoaded && !mapUnavailable) {
                Box(
                    Modifier.matchParentSize().background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }
            if (mapUnavailable && !mapLoaded) {
                Column(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(OficiosSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = "Mapa no disponible",
                        tint = OficiosColorWhite,
                    )
                    Text(
                        text = "No pudimos cargar el mapa. Usá tu ubicación actual para guardar el domicilio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            OficiosButton(
                text = "Usar mi ubicación",
                onClick = {
                    if (context.hasAnyLocationPermission()) {
                        hasLocationPermission = true
                        onUseCurrentLocation()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }
                },
                enabled = enabled && !isLocating,
                loading = isLocating,
                variant = OficiosButtonVariant.Secondary,
                modifier = Modifier.align(Alignment.BottomCenter).padding(OficiosSpacing.sm),
            )
        }
        Text(
            text = if (selected == null) {
                "Todavía no seleccionaste una ubicación."
            } else {
                "Ubicación confirmada · %.5f, %.5f".format(selected.latitude, selected.longitude)
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (selected == null) OficiosColorSecondaryText else OficiosColorAction,
        )
    }
}

@Composable
fun OficiosTrackingMap(
    destinationLatitude: Double,
    destinationLongitude: Double,
    workerLatitude: Double? = null,
    workerLongitude: Double? = null,
    modifier: Modifier = Modifier,
    showDestinationLabel: Boolean = false,
    onMapClick: (() -> Unit)? = null,
    mapContentDescription: String? = null,
    addressLabel: String = "",
    openInMapsLabel: String = "Abrir en Maps",
    unavailableDescription: String = "Mapa no disponible",
) {
    val context = LocalContext.current
    val mapsApiKey = remember { context.googleMapsApiKey() }
    var mapReady by remember { mutableStateOf(false) }
    var mapFailed by remember { mutableStateOf(mapsApiKey.isBlank()) }
    val destination = LatLng(destinationLatitude, destinationLongitude)
    val worker = workerLatitude?.let { lat -> workerLongitude?.let { lng -> LatLng(lat, lng) } }
    val focus = worker ?: destination
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(focus, if (worker == null) 16f else 14f)
    }

    LaunchedEffect(mapsApiKey) {
        if (mapsApiKey.isBlank()) {
            mapFailed = true
            return@LaunchedEffect
        }
        delay(MAP_LOAD_TIMEOUT_MILLIS)
        if (!mapReady) mapFailed = true
    }

    LaunchedEffect(focus, mapFailed) {
        if (!mapFailed) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(focus, if (worker == null) 16f else 14f))
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(Modifier.fillMaxSize()) {
            if (!mapFailed) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(),
                uiSettings = MapUiSettings(
                    compassEnabled = true,
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false,
                    scrollGesturesEnabled = onMapClick == null,
                ),
                onMapClick = { onMapClick?.invoke() },
                onMapLoaded = { mapReady = true },
            ) {
                Marker(
                    state = remember(destination) { MarkerState(position = destination) },
                    title = "Destino del servicio",
                )
                worker?.let {
                    Marker(
                        state = remember(it) { MarkerState(position = it) },
                        title = "Ubicación del trabajador",
                    )
                }
            }
            if (onMapClick != null && mapReady) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onMapClick)
                        .then(
                            if (mapContentDescription != null) {
                                Modifier.semantics { contentDescription = mapContentDescription }
                            } else {
                                Modifier
                            },
                        ),
                )
            }
            }
            if (!mapReady) {
                MapPlaceholder(
                    address = addressLabel,
                    openLabel = openInMapsLabel,
                    description = unavailableDescription,
                    onOpen = { onMapClick?.invoke() },
                )
            }
        }
    }
    if (showDestinationLabel || worker != null) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = OficiosSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (showDestinationLabel) {
                Text("Destino", style = MaterialTheme.typography.labelMedium, color = OficiosColorSecondaryText)
            }
            if (worker != null) {
                Text("Ubicación actualizada", style = MaterialTheme.typography.labelMedium, color = OficiosColorSuccessText)
            }
        }
    }
}

@Composable
private fun MapPlaceholder(
    address: String,
    openLabel: String,
    description: String,
    onOpen: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Place,
            contentDescription = description,
            tint = OficiosColorWhite,
            modifier = Modifier.size(OficiosComponentSize.iconLg),
        )
        if (address.isNotBlank()) {
            Text(
                text = address,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
        OficiosButton(
            text = openLabel,
            onClick = onOpen,
            variant = OficiosButtonVariant.Outlined,
            size = OficiosButtonSize.Prominent,
        )
    }
}

private fun android.content.Context.googleMapsApiKey(): String {
    return try {
        val info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        info.metaData?.getString("com.google.android.geo.API_KEY").orEmpty().trim()
    } catch (_: Exception) {
        ""
    }
}

private const val MAP_LOAD_TIMEOUT_MILLIS = 8_000L

private fun android.content.Context.hasAnyLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
