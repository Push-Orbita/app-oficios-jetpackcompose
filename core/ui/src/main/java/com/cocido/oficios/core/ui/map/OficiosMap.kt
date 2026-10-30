package com.cocido.oficios.core.ui.map

import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place as FilledPlace
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import kotlinx.coroutines.delay
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

internal val FormosaCenter = LatLng(-26.1849, -58.1731)

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
    var showMyLocation by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants.values.any { it } || context.hasAnyLocationPermission()
        if (granted) {
            showMyLocation = true
            onUseCurrentLocation()
        } else {
            onPermissionDenied()
        }
    }
    val selected = latitude?.let { lat -> longitude?.let { lng -> LatLng(lat, lng) } }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selected ?: FormosaCenter, if (selected == null) 12f else 16f)
    }
    val mapLoad = rememberMapLoadSession(mapsApiKey)

    LaunchedEffect(selected, mapLoad.state.loaded, mapLoad.state.unavailable) {
        val target = selected ?: return@LaunchedEffect
        cameraPositionState.followSelection(
            target = target,
            mapLoaded = mapLoad.state.loaded,
            mapUnavailable = mapLoad.state.unavailable,
        )
    }
    CenterPinSelectionEffect(
        cameraPositionState = cameraPositionState,
        enabled = enabled,
        onPinMoved = onLocationSelected,
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
        Text(
            text = "Ubicación exacta",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Mueva el mapa hasta que el pin quede en la entrada, o toque «Usar mi ubicación». Complete barrio y calle arriba para que coincidan con el pin.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PICKER_MAP_HEIGHT)
                .clip(RoundedCornerShape(16.dp))
                .then(mapLoad.visibilityModifier)
                .semantics { contentDescription = "Mapa para seleccionar la ubicación" },
        ) {
            if (mapLoad.state.showGoogleMap) {
                key(mapLoad.state.generation) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .then(if (enabled) Modifier.keepGesturesInsideMap() else Modifier),
                    ) {
                        GoogleMap(
                            modifier = Modifier.matchParentSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(isMyLocationEnabled = showMyLocation),
                            uiSettings = pickerUiSettings(interactive = enabled),
                            onMapClick = { point ->
                                if (enabled) {
                                    onLocationSelected(point.latitude, point.longitude)
                                }
                            },
                            onMapLoaded = mapLoad.onLoaded,
                        )
                    }
                }
                if (mapLoad.state.loaded) {
                    CenterPin(
                        lifted = cameraPositionState.isMoving &&
                            cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE,
                        dimmed = selected == null,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
            if (mapLoad.state.showSpinner) {
                Box(
                    Modifier.matchParentSize().background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }
            if (mapLoad.state.showFallback) {
                MapLoadFallback(
                    text = "No pudimos cargar el mapa. Use su ubicación actual para guardar el domicilio.",
                    onRetry = mapLoad.onRetry,
                )
            }
            OficiosButton(
                text = "Usar mi ubicación",
                onClick = {
                    if (context.hasAnyLocationPermission()) {
                        showMyLocation = true
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
                "Todavía no seleccionó una ubicación."
            } else {
                "Ubicación confirmada · %.5f, %.5f".format(selected.latitude, selected.longitude)
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (selected == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Ubicación de un domicilio escrito a mano. Nunca usa el GPS del dispositivo:
 * el pin sale del geocoding forward o de un arrastre explícito en Formosa.
 */
@Composable
fun OficiosAddressLocationPicker(
    latitude: Double?,
    longitude: Double?,
    showMap: Boolean,
    pinDraggable: Boolean,
    showLocateOnMap: Boolean,
    showConfirm: Boolean,
    isGeocoding: Boolean,
    onLocateOnMap: () -> Unit,
    onPinChanged: (Double, Double) -> Unit,
    onConfirmPin: () -> Unit,
    modifier: Modifier = Modifier,
    locateOnMapLabel: String = "Ubicar mi local en el mapa",
    confirmLabel: String = "Confirmar ubicación",
    confirmedLabel: String = "Ubicación confirmada",
    confirmed: Boolean = false,
    title: String = "Ubicación exacta",
    hint: String? = null,
) {
    val context = LocalContext.current
    val mapsApiKey = remember { context.googleMapsApiKey() }
    val selected = latitude?.let { lat -> longitude?.let { lng -> LatLng(lat, lng) } }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selected ?: FormosaCenter, if (selected == null) 12f else 16f)
    }
    val mapLoad = rememberMapLoadSession(mapsApiKey)
    val markerState = remember { MarkerState(position = selected ?: FormosaCenter) }

    LaunchedEffect(selected, mapLoad.state.loaded, mapLoad.state.unavailable) {
        val target = selected ?: FormosaCenter
        markerState.position = target
        if (selected == null) {
            cameraPositionState.moveSafely(
                target = target,
                zoom = 12f,
                mapLoaded = mapLoad.state.loaded,
                mapUnavailable = mapLoad.state.unavailable,
            )
        } else {
            cameraPositionState.followSelection(
                target = target,
                mapLoaded = mapLoad.state.loaded,
                mapUnavailable = mapLoad.state.unavailable,
            )
        }
    }
    CenterPinSelectionEffect(
        cameraPositionState = cameraPositionState,
        enabled = pinDraggable,
        onPinMoved = { lat, lng ->
            markerState.position = LatLng(lat, lng)
            onPinChanged(lat, lng)
        },
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = hint ?: "La ubicación se calcula a partir de la dirección. El GPS del teléfono no se usa.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (showMap) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PICKER_MAP_HEIGHT)
                    .clip(RoundedCornerShape(16.dp))
                    .then(mapLoad.visibilityModifier)
                    .semantics { contentDescription = "Mapa para confirmar la ubicación" },
            ) {
                if (mapLoad.state.showGoogleMap) {
                    key(mapLoad.state.generation) {
                        Box(
                            Modifier
                                .matchParentSize()
                                .then(if (pinDraggable) Modifier.keepGesturesInsideMap() else Modifier),
                        ) {
                            GoogleMap(
                                modifier = Modifier.matchParentSize(),
                                cameraPositionState = cameraPositionState,
                                properties = MapProperties(isMyLocationEnabled = false),
                                uiSettings = pickerUiSettings(interactive = pinDraggable),
                                onMapClick = { point ->
                                    if (pinDraggable) {
                                        markerState.position = point
                                        onPinChanged(point.latitude, point.longitude)
                                    }
                                },
                                onMapLoaded = mapLoad.onLoaded,
                            ) {
                                // Ajustando: el pin fijo al centro reemplaza al marker (se mueve el
                                // mapa, no el pin). En vista previa queda el marker de siempre.
                                if (!pinDraggable) {
                                    Marker(
                                        state = markerState,
                                        title = "Entrada del local",
                                    )
                                }
                            }
                        }
                    }
                    if (pinDraggable && mapLoad.state.loaded) {
                        CenterPin(
                            lifted = cameraPositionState.isMoving &&
                                cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE,
                            dimmed = false,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
                if (mapLoad.state.showSpinner) {
                    Box(
                        Modifier.matchParentSize().background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                }
                if (mapLoad.state.showFallback) {
                    MapLoadFallback(
                        text = "No pudimos cargar el mapa. Confirme la dirección escrita; no se usa el GPS.",
                        onRetry = mapLoad.onRetry,
                    )
                }
            }
            if (pinDraggable) {
                Text(
                    text = "Arrastre el mapa con un dedo y acerque con dos; el pin marca la entrada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected != null) {
                Text(
                    text = "Pin · %.5f, %.5f".format(selected.latitude, selected.longitude),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (showLocateOnMap) {
            OficiosButton(
                text = locateOnMapLabel,
                onClick = onLocateOnMap,
                enabled = !isGeocoding,
                loading = isGeocoding,
                variant = OficiosButtonVariant.Tonal,
                size = OficiosButtonSize.Form,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (showConfirm) {
            OficiosButton(
                text = confirmLabel,
                onClick = {
                    val pin = markerState.position
                    onPinChanged(pin.latitude, pin.longitude)
                    onConfirmPin()
                },
                enabled = !isGeocoding && (selected != null || pinDraggable),
                variant = OficiosButtonVariant.Tonal,
                size = OficiosButtonSize.Form,
                modifier = Modifier.fillMaxWidth(),
            )
        } else if (confirmed && selected != null) {
            Text(
                text = "$confirmedLabel · %.5f, %.5f".format(selected.latitude, selected.longitude),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
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
    val mapLoad = rememberMapLoadSession(mapsApiKey)
    val destination = LatLng(destinationLatitude, destinationLongitude)
    val worker = workerLatitude?.let { lat -> workerLongitude?.let { lng -> LatLng(lat, lng) } }
    val focus = worker ?: destination
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(focus, if (worker == null) 16f else 14f)
    }

    LaunchedEffect(focus, mapLoad.state.loaded, mapLoad.state.unavailable) {
        cameraPositionState.moveSafely(
            target = focus,
            zoom = if (worker == null) 16f else 14f,
            mapLoaded = mapLoad.state.loaded,
            mapUnavailable = mapLoad.state.unavailable,
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .then(mapLoad.visibilityModifier),
        ) {
            if (mapLoad.state.showGoogleMap) {
            key(mapLoad.state.generation) {
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
                onMapLoaded = mapLoad.onLoaded,
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
            }
            if (onMapClick != null && mapLoad.state.loaded) {
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
            if (mapLoad.state.showFallback) {
                MapLoadFallback(
                    text = "No pudimos cargar el mapa. Confirme la dirección escrita; no se usa el GPS.",
                    onRetry = mapLoad.onRetry,
                )
            } else if (!mapLoad.state.loaded) {
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
                Text("Destino", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (worker != null) {
                Text("Ubicación actualizada", style = MaterialTheme.typography.labelMedium, color = OficiosTheme.colors.success)
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
            tint = MaterialTheme.colorScheme.onSurface,
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
            size = OficiosButtonSize.Large,
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

@Composable
private fun rememberMapLoadSession(apiKey: String): MapLoadSession {
    val view = LocalView.current
    var state by remember(apiKey) {
        mutableStateOf(MapLoadState(unavailable = apiKey.isBlank()))
    }
    var visibleOnScreen by remember { mutableStateOf(false) }

    LaunchedEffect(apiKey, state.generation, visibleOnScreen, state.loaded) {
        if (apiKey.isBlank()) {
            state = state.copy(unavailable = true)
            return@LaunchedEffect
        }
        if (!shouldStartMapTimeout(apiKeyBlank = false, visibleOnScreen = visibleOnScreen)) {
            return@LaunchedEffect
        }
        delay(MAP_LOAD_TIMEOUT_MILLIS)
        state = state.onTimeout()
    }

    return MapLoadSession(
        state = state,
        visibilityModifier = Modifier.onGloballyPositioned { coords ->
            val bounds = coords.boundsInWindow()
            if (
                isMapOnScreen(
                    boundsLeft = bounds.left,
                    boundsTop = bounds.top,
                    boundsRight = bounds.right,
                    boundsBottom = bounds.bottom,
                    viewportWidth = view.width.toFloat(),
                    viewportHeight = view.height.toFloat(),
                )
            ) {
                visibleOnScreen = true
            }
        },
        onLoaded = { state = state.onLoaded() },
        onRetry = { state = state.retry(apiKeyBlank = apiKey.isBlank()) },
    )
}

private data class MapLoadSession(
    val state: MapLoadState,
    val visibilityModifier: Modifier,
    val onLoaded: () -> Unit,
    val onRetry: () -> Unit,
)

@Composable
private fun MapLoadFallback(
    text: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Place,
            contentDescription = "Mapa no disponible",
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        OficiosButton(
            text = "Reintentar",
            onClick = onRetry,
            variant = OficiosButtonVariant.Secondary,
            size = OficiosButtonSize.Large,
        )
    }
}

private fun android.content.Context.hasAnyLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private val PICKER_MAP_HEIGHT = 320.dp

/** Gestos de selección: pan y pinch fluidos; sin rotar ni inclinar para que el pin no confunda. */
private fun pickerUiSettings(interactive: Boolean) = MapUiSettings(
    compassEnabled = false,
    myLocationButtonEnabled = false,
    zoomControlsEnabled = false,
    mapToolbarEnabled = false,
    rotationGesturesEnabled = false,
    tiltGesturesEnabled = false,
    scrollGesturesEnabled = interactive,
    zoomGesturesEnabled = interactive,
    scrollGesturesEnabledDuringRotateOrZoom = interactive,
)

/**
 * Patrón "pin al centro": el usuario mueve el mapa y, cuando suelta, el centro de la cámara
 * pasa a ser la ubicación. Solo reacciona a gestos (no a animaciones nuestras), así seguir
 * la selección no vuelve a disparar otra selección.
 */
@Composable
private fun CenterPinSelectionEffect(
    cameraPositionState: CameraPositionState,
    enabled: Boolean,
    onPinMoved: (Double, Double) -> Unit,
) {
    val moving = cameraPositionState.isMoving
    LaunchedEffect(moving, enabled) {
        if (!enabled || moving) return@LaunchedEffect
        if (cameraPositionState.cameraMoveStartedReason != CameraMoveStartedReason.GESTURE) return@LaunchedEffect
        val target = cameraPositionState.position.target
        onPinMoved(target.latitude, target.longitude)
    }
}

/** Pin fijo al centro del mapa; se levanta mientras se arrastra para que se vea dónde cae. */
@Composable
private fun CenterPin(
    lifted: Boolean,
    dimmed: Boolean,
    modifier: Modifier = Modifier,
) {
    val lift by animateDpAsState(targetValue = if (lifted) 10.dp else 0.dp, label = "pinLift")
    val alpha by animateFloatAsState(targetValue = if (dimmed) 0.75f else 1f, label = "pinAlpha")
    Box(modifier = modifier.size(CENTER_PIN_SIZE * 2), contentAlignment = Alignment.Center) {
        // Sombra en el punto exacto: la punta del pin cae acá.
        Box(
            Modifier
                .size(if (lifted) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)),
        )
        Icon(
            imageVector = Icons.Filled.FilledPlace,
            contentDescription = "Pin de ubicación",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .offset(y = -(CENTER_PIN_SIZE / 2) - lift)
                .size(CENTER_PIN_SIZE)
                .alpha(alpha),
        )
    }
}

private val CENTER_PIN_SIZE = 40.dp
