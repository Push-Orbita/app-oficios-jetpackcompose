package com.cocido.oficios.feature.home.orders

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.avatar.OficiosAvatar
import com.cocido.oficios.feature.home.R
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.chips.OficiosChip
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosSkeleton
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.component.motion.oficiosShake
import com.cocido.oficios.core.designsystem.component.motion.rememberOficiosRejectShake
import com.cocido.oficios.core.designsystem.theme.OficiosColorAccent
import com.cocido.oficios.core.designsystem.theme.OficiosColorAction
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosMotion
import com.cocido.oficios.core.designsystem.theme.oficiosTween
import com.cocido.oficios.core.ui.map.OficiosTrackingMap
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    uiState: OrderDetailUiState,
    onEvent: (OrderDetailEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = { OficiosBackTopBar(title = stringResource(R.string.orders_detail_title), onBack = onNavigateBack) },
    ) { paddingValues ->
        OficiosStateContent(targetState = uiState, modifier = Modifier.padding(paddingValues)) { state ->
            when (state) {
                OrderDetailUiState.Loading -> OrderDetailSkeleton(
                    message = stringResource(R.string.orders_detail_loading),
                    modifier = Modifier.fillMaxSize(),
                )
                is OrderDetailUiState.Error -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onEvent(OrderDetailEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            OficiosErrorState(
                                title = stringResource(R.string.orders_detail_error_title),
                                description = state.message.ifBlank { stringResource(R.string.orders_detail_error_description) },
                                onRetry = { onEvent(OrderDetailEvent.Retry) },
                                modifier = Modifier.fillParentMaxSize(),
                            )
                        }
                    }
                }
                is OrderDetailUiState.Success -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onEvent(OrderDetailEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    OrderDetailContent(
                        state = state,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderDetailSkeleton(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg)
            .semantics { contentDescription = message },
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        OficiosSkeleton(modifier = Modifier.size(width = OficiosComponentSize.skeletonBlockMd, height = OficiosComponentSize.iconLg))
        Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
            repeat(5) { OficiosSkeleton(modifier = Modifier.size(OficiosComponentSize.iconLg), shape = CircleShape) }
        }
        OficiosSkeleton(modifier = Modifier.fillMaxWidth().height(OficiosComponentSize.avatarSettings))
        OficiosSkeleton(modifier = Modifier.fillMaxWidth().height(OficiosComponentSize.skeletonBlockMd))
        OficiosSkeleton(modifier = Modifier.fillMaxWidth().height(OficiosComponentSize.skeletonBlockLg))
    }
}

@Composable
private fun OrderDetailContent(
    state: OrderDetailUiState.Success,
    onEvent: (OrderDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val order = state.order
    val isWorker = state.viewerRole == UserRole.PROVIDER
    val context = LocalContext.current
    val canShowPrivateDetails = !isWorker || order.status == ServiceRequestStatus.ACCEPTED
    var showRejectDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showFinalizeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
            ) {
                if (orderTimeline(order.status, order.bookingStatus).isNotEmpty()) {
                    ServiceOrderStatusLabel(status = order.status, bookingStatus = order.bookingStatus)
                }
                order.scheduledLabel?.let { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            OrderProgress(status = order.status, bookingStatus = order.bookingStatus)
            cancelledByLabelRes(order.cancelledBy)?.let { label ->
                Text(stringResource(label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            order.cancellationReason?.takeIf { it.isNotBlank() }?.let { reason ->
                Text(reason, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        if (isWorker && order.clientName.isNotBlank()) {
            OficiosCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = OficiosComponentSize.buttonHeight),
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OficiosAvatar(name = order.clientName, imageUrl = order.clientAvatarUrl, circular = true)
                    Text(
                        text = order.clientName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (!isWorker && order.workerId != 0L) {
            val trade = listOf(order.rubroLabel, order.subRubroLabel).filter { it.isNotBlank() }.joinToString(" · ")
            OficiosCard(onClick = { onEvent(OrderDetailEvent.OpenWorkerProfile) }, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = OficiosComponentSize.buttonHeight),
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OficiosAvatar(name = order.workerName, imageUrl = order.workerAvatarUrl, circular = true)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(order.workerName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        if (trade.isNotBlank()) {
                            Text(trade, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.orders_open_worker),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        OficiosCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
                SectionTitle(stringResource(R.string.orders_section_details))
                InfoRow(
                    icon = Icons.Outlined.Place,
                    contentDescription = stringResource(R.string.orders_address),
                    text = if (canShowPrivateDetails) {
                        "${stringResource(R.string.orders_address)}: ${order.addressLabel}"
                    } else {
                        stringResource(R.string.orders_address_hidden)
                    },
                )
                val service = listOf(order.rubroLabel, order.subRubroLabel).filter { it.isNotBlank() }.joinToString(" · ")
                if (service.isNotBlank()) {
                    InfoRow(Icons.Outlined.HomeRepairService, stringResource(R.string.orders_service), service)
                }
                if (isWorker && canShowPrivateDetails && order.contactName.isNotBlank()) {
                    InfoRow(
                        Icons.Outlined.Phone,
                        stringResource(R.string.orders_contact),
                        "${stringResource(R.string.orders_contact)}: ${order.contactName} · ${order.contactPhone}",
                    )
                }
                if (order.description.isNotBlank()) {
                    InfoRow(
                        Icons.Outlined.Description,
                        stringResource(R.string.orders_description),
                        "${stringResource(R.string.orders_description)}: ${order.description}",
                    )
                }
                order.bookingType?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(Icons.Outlined.Category, stringResource(R.string.orders_type), "${stringResource(R.string.orders_type)}: $it")
                }
            }
        }

        val addressLatitude = order.addressLatitude
        val addressLongitude = order.addressLongitude
        if (canShowPrivateDetails && addressLatitude != null && addressLongitude != null) {
            Column(
                modifier = Modifier.clickable {
                    openOrderMap(context, addressLatitude, addressLongitude, order.addressLabel)
                },
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
            ) {
                SectionTitle(
                    stringResource(
                        if (state.tracking != null && !isWorker) {
                            R.string.orders_map_worker_location
                        } else {
                            R.string.orders_map_service_location
                        },
                    ),
                )
                OficiosTrackingMap(
                    destinationLatitude = addressLatitude,
                    destinationLongitude = addressLongitude,
                    workerLatitude = if (!isWorker) state.tracking?.location?.latitude else null,
                    workerLongitude = if (!isWorker) state.tracking?.location?.longitude else null,
                    onMapClick = { openOrderMap(context, addressLatitude, addressLongitude, order.addressLabel) },
                    mapContentDescription = stringResource(R.string.orders_open_map),
                    addressLabel = order.addressLabel,
                    openInMapsLabel = stringResource(R.string.orders_open_in_maps),
                    unavailableDescription = stringResource(R.string.orders_map_unavailable),
                )
                if (!isWorker && order.bookingStatus == BookingStatus.EN_ROUTE && state.tracking == null) {
                    InfoText(stringResource(R.string.orders_tracking_waiting))
                }
            }
        }

        when {
            order.status == ServiceRequestStatus.PENDING && isWorker -> {
                Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md), modifier = Modifier.fillMaxWidth()) {
                    OficiosButton(
                        text = stringResource(R.string.orders_reject),
                        onClick = { showRejectDialog = true },
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Outlined.Close,
                        variant = OficiosButtonVariant.OutlinedDanger,
                        size = OficiosButtonSize.Medium,
                        enabled = !state.isActionInProgress,
                    )
                    OficiosButton(
                        text = stringResource(R.string.orders_accept),
                        onClick = { onEvent(OrderDetailEvent.Accept) },
                        modifier = Modifier.weight(1f),
                        variant = OficiosButtonVariant.Secondary,
                        size = OficiosButtonSize.Medium,
                        enabled = !state.isActionInProgress,
                    )
                }
                OficiosButton(
                    text = stringResource(R.string.orders_reschedule),
                    onClick = { onEvent(OrderDetailEvent.OpenCounterOffer) },
                    variant = OficiosButtonVariant.Primary,
                    size = OficiosButtonSize.Large,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isActionInProgress,
                )
            }

            order.status == ServiceRequestStatus.PENDING && !isWorker -> {
                InfoText(stringResource(R.string.orders_waiting_worker))
                CancelOrderButton(enabled = !state.isActionInProgress, onClick = { showCancelDialog = true })
            }

            order.status == ServiceRequestStatus.COUNTER_OFFER && order.pendingCounterOffer != null -> {
                order.pendingCounterOffer?.let { offer ->
                    OficiosCard {
                        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                            Text(
                                text = stringResource(R.string.orders_worker_proposed),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(text = stringResource(R.string.orders_new_date, offer.proposedLabel), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            offer.proposedPrice?.let {
                                Text(text = stringResource(R.string.orders_new_price, it), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                if (!isWorker) {
                    Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.md), modifier = Modifier.fillMaxWidth()) {
                        OficiosButton(
                            text = stringResource(R.string.orders_reject),
                            onClick = { onEvent(OrderDetailEvent.RejectCounterOffer) },
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Outlined.Close,
                            variant = OficiosButtonVariant.OutlinedDanger,
                            size = OficiosButtonSize.Medium,
                            enabled = !state.isActionInProgress,
                        )
                        OficiosButton(
                            text = stringResource(R.string.orders_accept),
                            onClick = { onEvent(OrderDetailEvent.AcceptCounterOffer) },
                            modifier = Modifier.weight(1f),
                            variant = OficiosButtonVariant.Secondary,
                            size = OficiosButtonSize.Medium,
                            enabled = !state.isActionInProgress,
                        )
                    }
                } else {
                    InfoText(stringResource(R.string.orders_waiting_counteroffer))
                }
            }

            // pendingCounterOffer nulo con status COUNTER_OFFER sería un dato inconsistente
            // del backend (la contrapropuesta debería venir siempre en ese estado) — igual se
            // cubre para no dejar la pantalla en blanco si llega a pasar.
            order.status == ServiceRequestStatus.COUNTER_OFFER -> {
                InfoText(stringResource(R.string.orders_counteroffer_error))
            }

            order.status == ServiceRequestStatus.ACCEPTED -> when (order.bookingStatus) {
                BookingStatus.CONFIRMED -> {
                    if (isWorker) {
                        ContactButton(
                            label = stringResource(R.string.orders_contact_client),
                            phone = order.contactPhone,
                            onClick = { openWorkerContact(context, order.contactPhone) },
                            size = OficiosButtonSize.Medium,
                        )
                        OficiosButton(
                            text = stringResource(R.string.orders_on_the_way),
                            onClick = { onEvent(OrderDetailEvent.MarkEnRoute) },
                            modifier = Modifier.fillMaxWidth(),
                            variant = OficiosButtonVariant.Secondary,
                            size = OficiosButtonSize.Large,
                            enabled = !state.isActionInProgress,
                        )
                    } else {
                        ContactButton(
                            label = stringResource(R.string.orders_contact_worker),
                            phone = order.workerPhone.orEmpty(),
                            onClick = { openWorkerContact(context, order.workerPhone.orEmpty()) },
                        )
                    }
                }
                BookingStatus.EN_ROUTE -> {
                    if (isWorker) {
                        InfoText(stringResource(R.string.orders_marked_on_the_way))
                        ContactButton(
                            label = stringResource(R.string.orders_contact_client),
                            phone = order.contactPhone,
                            onClick = { openWorkerContact(context, order.contactPhone) },
                            size = OficiosButtonSize.Medium,
                        )
                        OficiosButton(
                            text = stringResource(R.string.orders_mark_arrival),
                            onClick = { onEvent(OrderDetailEvent.MarkArrived) },
                            modifier = Modifier.fillMaxWidth(),
                            variant = OficiosButtonVariant.Secondary,
                            size = OficiosButtonSize.Large,
                            enabled = !state.isActionInProgress,
                        )
                    } else {
                        ContactButton(
                            label = stringResource(R.string.orders_contact_worker),
                            phone = order.workerPhone.orEmpty(),
                            onClick = { openWorkerContact(context, order.workerPhone.orEmpty()) },
                        )
                    }
                }
                BookingStatus.IN_PROGRESS -> {
                    if (!isWorker) {
                        CompletionCodeCard(
                            code = order.completionCode,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        OficiosButton(
                            text = stringResource(R.string.orders_finish),
                            onClick = { showFinalizeDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            variant = OficiosButtonVariant.Secondary,
                            size = OficiosButtonSize.Large,
                            enabled = !state.isActionInProgress,
                        )
                    }
                }
                BookingStatus.FINISHED -> {
                    if (!isWorker) {
                        if (order.alreadyRated) {
                            SubmittedRating(quality = order.qualityScore, punctuality = order.punctualityScore)
                        } else {
                            OficiosButton(
                                text = stringResource(R.string.orders_rate_worker),
                                onClick = { onEvent(OrderDetailEvent.GoToRate) },
                                modifier = Modifier.fillMaxWidth(),
                                variant = OficiosButtonVariant.Secondary,
                                size = OficiosButtonSize.Large,
                            )
                        }
                    } else {
                        InfoText(stringResource(R.string.orders_finished))
                    }
                }
                // bookingStatus fuera de lo esperado para un pedido ACCEPTED (ej. PENDING o
                // CANCELLED) sería inconsistente, pero se muestra algo accionable en vez de
                // dejar la pantalla en blanco.
                else -> {
                    InfoText(stringResource(R.string.orders_unknown_booking))
                    OficiosButton(
                        text = stringResource(R.string.orders_retry),
                        onClick = { onEvent(OrderDetailEvent.Retry) },
                        variant = OficiosButtonVariant.Secondary,
                        size = OficiosButtonSize.Large,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            order.status == ServiceRequestStatus.CANCELLED -> {
                val reason = order.rejectionReason
                InfoText(if (reason.isNullOrBlank()) stringResource(R.string.orders_cancelled) else stringResource(R.string.orders_rejected_reason, reason))
            }

            order.status == ServiceRequestStatus.EXPIRED -> {
                InfoText(stringResource(R.string.orders_expired))
            }

            else -> {
                InfoText(stringResource(R.string.orders_unknown_status))
            }
        }

        if (order.canCancelBooking && order.status != ServiceRequestStatus.PENDING && order.status != ServiceRequestStatus.CANCELLED) {
            CancelOrderButton(
                enabled = !state.isActionInProgress,
                onClick = { showCancelDialog = true },
                size = OficiosButtonSize.Medium,
            )
        }
    }

    if (showRejectDialog) {
        ReasonDialog(
            title = stringResource(R.string.orders_reject_title),
            confirmLabel = stringResource(R.string.orders_reject),
            onConfirm = {
                onEvent(OrderDetailEvent.Reject(it))
                showRejectDialog = false
            },
            onDismiss = { showRejectDialog = false },
        )
    }

    if (showCancelDialog) {
        OficiosDialog(
            title = stringResource(R.string.orders_cancel_title),
            message = stringResource(R.string.orders_cancel_message),
            confirmLabel = stringResource(R.string.orders_cancel_confirm),
            dismissLabel = stringResource(R.string.orders_cancel_back),
            isDangerous = true,
            actionSize = OficiosButtonSize.Large,
            onConfirm = {
                if (order.canCancelBooking && order.status != ServiceRequestStatus.PENDING) {
                    onEvent(OrderDetailEvent.CancelConfirmed(null))
                } else {
                    onEvent(OrderDetailEvent.CancelBeforeAcceptance)
                }
                showCancelDialog = false
            },
            onDismiss = { showCancelDialog = false },
        )
    }

    if (showFinalizeDialog && order.bookingStatus != BookingStatus.FINISHED) {
        FinalizeCodeDialog(
            error = state.completionCodeError,
            onConfirm = { code -> onEvent(OrderDetailEvent.FinalizeWithCode(code)) },
            onDismiss = {
                showFinalizeDialog = false
                onEvent(OrderDetailEvent.ClearCompletionError)
            },
        )
    }
}

@Composable
private fun OrderProgress(status: ServiceRequestStatus, bookingStatus: BookingStatus?) {
    val steps = orderTimeline(status, bookingStatus)
    if (steps.isEmpty()) {
        OrderClosedBanner(status = status, bookingStatus = bookingStatus)
        return
    }
    val phases = orderTimelinePhases(status, bookingStatus)
    val active = orderTimelineActiveIndex(status, bookingStatus)
    val progress by animateFloatAsState(
        targetValue = active.toFloat(),
        animationSpec = oficiosTween(OficiosMotion.DurationLong),
        label = "OrderProgress",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.Top,
    ) {
        steps.forEachIndexed { index, step ->
            if (index > 0) {
                TimelineConnector(fill = { (progress - (index - 1)).coerceIn(0f, 1f) })
            }
            TimelineStep(step = step, phase = phases[index], index = index, total = steps.size)
        }
    }
}

@Composable
private fun OrderClosedBanner(status: ServiceRequestStatus, bookingStatus: BookingStatus?) {
    val expired = status == ServiceRequestStatus.EXPIRED
    val visual = orderStatusVisual(status, bookingStatus)
    OficiosChip(
        label = stringResource(if (expired) R.string.orders_banner_expired else R.string.orders_banner_cancelled),
        containerColor = visual.container,
        contentColor = visual.content,
        borderColor = visual.container,
        leadingIcon = visual.icon,
    )
}

@Composable
private fun TimelineConnector(fill: () -> Float) {
    Box(
        modifier = Modifier
            .padding(top = (OficiosComponentSize.iconMd - 2.dp) / 2)
            .width(OficiosSpacing.md)
            .height(2.dp)
            .background(OficiosColorWhite.copy(alpha = 0.4f))
            .drawBehind {
                drawRect(color = OficiosColorWhite, size = size.copy(width = size.width * fill()))
            },
    )
}

@Composable
private fun TimelineStep(step: OrderTimelineStep, phase: OrderTimelinePhase, index: Int, total: Int) {
    val label = when (step) {
        OrderTimelineStep.REQUESTED -> stringResource(R.string.orders_step_requested)
        OrderTimelineStep.CONFIRMED -> stringResource(R.string.orders_step_confirmed)
        OrderTimelineStep.EN_ROUTE -> stringResource(R.string.orders_step_en_route)
        OrderTimelineStep.IN_PROGRESS -> stringResource(R.string.orders_step_in_progress)
        OrderTimelineStep.FINISHED -> stringResource(R.string.orders_step_finished)
    }
    val stateLabel = stringResource(
        when (phase) {
            OrderTimelinePhase.Done -> R.string.orders_step_state_done
            OrderTimelinePhase.Current -> R.string.orders_step_state_current
            OrderTimelinePhase.Upcoming -> R.string.orders_step_state_upcoming
        },
    )
    val description = stringResource(R.string.orders_step_a11y, index + 1, total, label, stateLabel)
    val colorSpec = oficiosTween<Color>(OficiosMotion.DurationLong)
    val circle by animateColorAsState(
        targetValue = when (phase) {
            OrderTimelinePhase.Done -> OficiosColorAction
            OrderTimelinePhase.Current -> OficiosColorAccent
            OrderTimelinePhase.Upcoming -> OficiosColorOutlineFill
        },
        animationSpec = colorSpec,
        label = "TimelineStepCircle",
    )
    val iconTint by animateColorAsState(
        targetValue = when (phase) {
            OrderTimelinePhase.Done -> OficiosColorWhite
            OrderTimelinePhase.Current -> OficiosColorBackground
            OrderTimelinePhase.Upcoming -> OficiosColorWhite.copy(alpha = 0.72f)
        },
        animationSpec = colorSpec,
        label = "TimelineStepIcon",
    )
    val labelColor by animateColorAsState(
        targetValue = if (phase == OrderTimelinePhase.Upcoming) {
            OficiosColorWhite.copy(alpha = 0.72f)
        } else {
            OficiosColorWhite
        },
        animationSpec = colorSpec,
        label = "TimelineStepLabel",
    )
    Column(
        modifier = Modifier
            .widthIn(min = OficiosComponentSize.buttonHeight)
            .clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(OficiosComponentSize.iconMd)
                .clip(CircleShape)
                .background(circle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (phase == OrderTimelinePhase.Done) Icons.Outlined.Check else orderTimelineStepIcon(step),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(OficiosComponentSize.iconSm),
            )
        }
        Text(
            text = label,
            modifier = Modifier.padding(top = OficiosSpacing.xxs),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.sp,
                hyphens = Hyphens.None,
            ),
            color = labelColor,
            textAlign = TextAlign.Center,
            softWrap = true,
            maxLines = 2,
        )
    }
}

private fun openWorkerContact(context: android.content.Context, phone: String) {
    val trimmed = phone.trim()
    val digits = trimmed.filter { it.isDigit() }
    if (digits.isBlank()) return
    val whatsapp = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits"))
    val dial = if (trimmed.startsWith("+")) "+$digits" else digits
    if (whatsapp.resolveActivity(context.packageManager) != null) {
        context.startActivity(whatsapp)
    } else {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$dial")))
    }
}

private fun openOrderMap(context: android.content.Context, latitude: Double, longitude: Double, label: String) {
    val geo = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
    val mapIntent = Intent(Intent.ACTION_VIEW, geo)
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")),
        )
    }
}

@Composable
private fun CancelOrderButton(
    enabled: Boolean,
    onClick: () -> Unit,
    size: OficiosButtonSize = OficiosButtonSize.Large,
) {
    OficiosButton(
        text = stringResource(R.string.orders_cancel),
        onClick = onClick,
        leadingIcon = Icons.Outlined.Close,
        variant = OficiosButtonVariant.OutlinedDanger,
        size = size,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun InfoText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun InfoRow(icon: ImageVector, contentDescription: String, text: String) {
    Row(
        modifier = Modifier.heightIn(min = OficiosComponentSize.buttonHeight),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(OficiosComponentSize.iconSm),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ContactButton(
    label: String,
    phone: String,
    onClick: () -> Unit,
    size: OficiosButtonSize = OficiosButtonSize.Large,
) {
    if (phone.isBlank()) return
    OficiosButton(
        text = label,
        onClick = onClick,
        leadingIcon = Icons.Outlined.Phone,
        variant = OficiosButtonVariant.Secondary,
        size = size,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CompletionCodeCard(
    code: String?,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    OficiosCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
            SectionTitle(stringResource(R.string.orders_code_title))
            if (code.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.orders_code_missing),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                Text(
                    text = code,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(if (copied) R.string.orders_code_copied else R.string.orders_code_share),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                OficiosButton(
                    text = stringResource(if (copied) R.string.orders_code_copied else R.string.orders_code_copy),
                    onClick = {
                        clipboard.setText(AnnotatedString(code))
                        copied = true
                    },
                    leadingIcon = Icons.Outlined.ContentCopy,
                    variant = OficiosButtonVariant.Outlined,
                    size = OficiosButtonSize.Medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SubmittedRating(quality: Int?, punctuality: Int?) {
    Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
        SectionTitle(stringResource(R.string.orders_already_rated))
        qualityStars(quality)?.let { stars ->
            RatingLine(label = stringResource(R.string.orders_rating_quality), stars = stars)
        }
        punctualityStars(punctuality)?.let { stars ->
            RatingLine(label = stringResource(R.string.orders_rating_punctuality), stars = stars)
        }
    }
}

@Composable
private fun RatingLine(label: String, stars: Int) {
    val description = stringResource(R.string.orders_rating_stars, stars)
    Row(
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(modifier = Modifier.clearAndSetSemantics { contentDescription = "$label, $description" }) {
            repeat(stars) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = OficiosColorAccent,
                    modifier = Modifier.size(OficiosComponentSize.iconMd),
                )
            }
        }
    }
}

@Composable
private fun FinalizeCodeDialog(
    error: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    var visibleError by remember(error) { mutableStateOf(error) }
    val shake = rememberOficiosRejectShake(rejectionKey = error)
    OficiosDialog(
        title = stringResource(R.string.orders_finish),
        message = stringResource(R.string.orders_finalize_message),
        confirmLabel = stringResource(R.string.orders_finalize_confirm),
        dismissLabel = stringResource(R.string.orders_dialog_cancel),
        confirmEnabled = code.length == 6,
        onConfirm = { onConfirm(code.trim()) },
        onDismiss = onDismiss,
        content = {
            OficiosTextField(
                value = code,
                onValueChange = { input ->
                    visibleError = null
                    if (input.length <= 6) code = input.filter { ch -> ch.isDigit() }
                },
                label = stringResource(R.string.orders_code_label),
                modifier = Modifier.oficiosShake(shake),
                isError = visibleError != null,
                supportingText = visibleError,
            )
        },
    )
}

@Composable
private fun ReasonDialog(
    title: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by remember { mutableStateOf("") }
    OficiosDialog(
        title = title,
        message = stringResource(R.string.orders_reason_message),
        confirmLabel = confirmLabel,
        isDangerous = true,
        confirmEnabled = reason.isNotBlank(),
        onConfirm = { onConfirm(reason) },
        onDismiss = onDismiss,
        content = {
            OficiosTextField(
                value = reason,
                onValueChange = { reason = it },
                label = stringResource(R.string.orders_reason_label),
            )
        },
    )
}
