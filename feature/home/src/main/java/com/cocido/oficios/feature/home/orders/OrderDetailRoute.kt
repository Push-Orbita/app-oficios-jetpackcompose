package com.cocido.oficios.feature.home.orders

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosSnackbarController
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.UserRole

@Composable
fun OrderDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToWorker: (Long) -> Unit = {},
    onNavigateToCounterOffer: (Long) -> Unit,
    onNavigateToRate: (Long, Long, String) -> Unit,
    onShowMessage: (String) -> Unit,
    onStartTracking: (Long) -> Unit,
    onStopTracking: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = LocalOficiosSnackbarController.current
    val context = LocalContext.current
    var pendingLocationAction by remember { mutableStateOf<TrackingLocationAction?>(null) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val action = pendingLocationAction ?: return@rememberLauncherForActivityResult
        val hasFine = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        viewModel.onEvent(
            if (hasFine) OrderDetailEvent.LocationPermissionGranted(action)
            else OrderDetailEvent.LocationPermissionDenied(action),
        )
        pendingLocationAction = null
    }

    // Reagendar/Puntuar navegan a otra pantalla y vuelven con popBackStack — el ViewModel de
    // esta ruta no se recrea, así que hay que refrescar manualmente al resumir.
    ReloadOnResume { viewModel.onEvent(OrderDetailEvent.Retry) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OrderDetailEffect.ShowMessage -> snackbar.showError(effect.message)
                is OrderDetailEffect.NavigateToCounterOffer -> onNavigateToCounterOffer(effect.orderId)
                is OrderDetailEffect.NavigateToRate -> onNavigateToRate(effect.bookingId, effect.workerId, effect.workerName)
                is OrderDetailEffect.NavigateToWorker -> onNavigateToWorker(effect.workerId)
                is OrderDetailEffect.RequestPreciseLocation -> {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        viewModel.onEvent(OrderDetailEvent.LocationPermissionGranted(effect.action))
                    } else {
                        pendingLocationAction = effect.action
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }
                }
                is OrderDetailEffect.StartForegroundTracking -> onStartTracking(effect.bookingId)
                OrderDetailEffect.StopForegroundTracking -> onStopTracking()
            }
        }
    }

    LaunchedEffect(uiState) {
        val state = uiState as? OrderDetailUiState.Success ?: return@LaunchedEffect
        val bookingId = state.order.bookingId ?: return@LaunchedEffect
        val canResume = state.viewerRole == UserRole.PROVIDER &&
            state.order.bookingStatus == BookingStatus.EN_ROUTE &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (canResume) onStartTracking(bookingId)
    }

    OrderDetailScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
private fun ReloadOnResume(onReload: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onReload()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
