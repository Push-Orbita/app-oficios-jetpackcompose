package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.core.ui.lifecycle.RefreshOnDataChange
import com.cocido.oficios.core.common.sync.DataTopic
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosToastController
import com.cocido.oficios.core.designsystem.component.feedback.OficiosAnimatedSuccessOverlay
import com.cocido.oficios.core.designsystem.theme.OficiosMotion
import com.cocido.oficios.core.designsystem.theme.oficiosTween
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import com.cocido.oficios.core.ui.permission.PermissionGrantPredicate
import com.cocido.oficios.core.ui.permission.RuntimePermissionLabels
import com.cocido.oficios.core.ui.permission.rememberRuntimePermissionRequest
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.core.ui.R as UiR
import kotlinx.coroutines.delay

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
    val toastController = LocalOficiosToastController.current
    val context = LocalContext.current
    var pendingLocationAction by remember { mutableStateOf<TrackingLocationAction?>(null) }
    var showCompletionSuccess by remember { mutableStateOf(false) }

    val trackingPermission = rememberRuntimePermissionRequest(
        permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ),
        labels = RuntimePermissionLabels(
            rationaleTitle = stringResource(UiR.string.permission_tracking_rationale_title),
            rationaleMessage = stringResource(UiR.string.permission_tracking_rationale_message),
            rationaleConfirm = stringResource(UiR.string.permission_continue),
            deniedTitle = stringResource(UiR.string.permission_tracking_denied_title),
            deniedMessage = stringResource(UiR.string.permission_tracking_denied_message),
            openSettings = stringResource(UiR.string.permission_open_settings),
            dismiss = stringResource(UiR.string.permission_not_now),
        ),
        onGranted = {
            val action = pendingLocationAction
            pendingLocationAction = null
            if (action != null) {
                viewModel.onEvent(OrderDetailEvent.LocationPermissionGranted(action))
            }
        },
        onDenied = {
            val action = pendingLocationAction
            pendingLocationAction = null
            if (action != null) {
                viewModel.onEvent(OrderDetailEvent.LocationPermissionDenied(action))
            }
        },
        isSatisfied = PermissionGrantPredicate { grants ->
            grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
        },
    )

    // Reagendar/Puntuar navegan a otra pantalla y vuelven con popBackStack — el ViewModel de
    // esta ruta no se recrea, así que hay que refrescar manualmente al resumir.
    ReloadOnResume { viewModel.onEvent(OrderDetailEvent.Refresh) }
    RefreshOnDataChange(DataTopic.ORDERS) { viewModel.onEvent(OrderDetailEvent.Refresh) }

    // Cliente EN_CURSO: el código de finalización se renueva solo mientras la pantalla está visible.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.keepCompletionCodeFresh()
        }
    }

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.keepOrderStatusFresh()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OrderDetailEffect.ShowMessage -> toastController.showError(effect.message)
                is OrderDetailEffect.ShowSuccess -> toastController.showSuccess(effect.message)
                is OrderDetailEffect.NavigateToCounterOffer -> onNavigateToCounterOffer(effect.orderId)
                is OrderDetailEffect.NavigateToRate -> onNavigateToRate(effect.bookingId, effect.workerId, effect.workerName)
                is OrderDetailEffect.NavigateToWorker -> onNavigateToWorker(effect.workerId)
                is OrderDetailEffect.RequestPreciseLocation -> {
                    pendingLocationAction = effect.action
                    trackingPermission.launch()
                }
                is OrderDetailEffect.StartForegroundTracking -> onStartTracking(effect.bookingId)
                OrderDetailEffect.StopForegroundTracking -> onStopTracking()
                OrderDetailEffect.CompletionConfirmed -> showCompletionSuccess = true
            }
        }
    }

    LaunchedEffect(showCompletionSuccess) {
        if (showCompletionSuccess) {
            delay(COMPLETION_SUCCESS_HOLD_MILLIS)
            showCompletionSuccess = false
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

    Box(modifier = modifier) {
        OrderDetailScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
        )
        val fade = oficiosTween<Float>(OficiosMotion.DurationMedium)
        AnimatedVisibility(
            visible = showCompletionSuccess,
            enter = fadeIn(fade),
            exit = fadeOut(fade),
        ) {
            OficiosAnimatedSuccessOverlay(
                title = stringResource(R.string.orders_finalize_success_title),
                description = stringResource(R.string.orders_finalize_success_description),
                modifier = Modifier.clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = { showCompletionSuccess = false },
                ),
            )
        }
    }
}

private const val COMPLETION_SUCCESS_HOLD_MILLIS = 1_600L

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
