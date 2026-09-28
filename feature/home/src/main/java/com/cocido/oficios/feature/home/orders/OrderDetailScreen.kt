package com.cocido.oficios.feature.home.orders

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosColorAccent
import com.cocido.oficios.core.designsystem.theme.OficiosColorAction
import com.cocido.oficios.core.designsystem.theme.OficiosColorBackground
import com.cocido.oficios.core.designsystem.theme.OficiosColorOutlineFill
import com.cocido.oficios.core.designsystem.theme.OficiosColorWhite
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.ui.map.OficiosTrackingMap
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.model.UserRole

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
        when (uiState) {
            OrderDetailUiState.Loading -> OficiosLoadingState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                message = stringResource(R.string.orders_detail_loading),
            )
            is OrderDetailUiState.Error -> OficiosErrorState(
                title = stringResource(R.string.orders_detail_error_title),
                description = uiState.message,
                onRetry = { onEvent(OrderDetailEvent.Retry) },
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )
            is OrderDetailUiState.Success -> OrderDetailContent(
                state = uiState,
                onEvent = onEvent,
                modifier = Modifier.padding(paddingValues),
            )
        }
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
    val canShowPrivateDetails = !isWorker || order.status == ServiceRequestStatus.ACCEPTED
    var showRejectDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showFinalizeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OficiosSpacing.lg, vertical = OficiosSpacing.md),
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
    ) {
        val visual = orderStatusVisual(order.status, order.bookingStatus)
        OficiosCard {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                ServiceOrderStatusLabel(status = order.status, bookingStatus = order.bookingStatus)
                Text(
                    text = stringResource(visual.explanationRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                OrderTimeline(status = order.status, bookingStatus = order.bookingStatus)
                cancelledByLabelRes(order.cancelledBy)?.let { label ->
                    Text(stringResource(label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                order.cancellationReason?.takeIf { it.isNotBlank() }?.let { reason ->
                    Text(reason, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        if (!isWorker && order.workerId != 0L) {
            OficiosCard(onClick = { onEvent(OrderDetailEvent.OpenWorkerProfile) }, modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    OficiosAvatar(name = order.workerName, imageUrl = order.workerAvatarUrl, circular = true)
                    Column {
                        Text(stringResource(R.string.orders_worker), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(order.workerName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("${order.rubroLabel} · ${order.subRubroLabel}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        OficiosCard {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm)) {
                order.scheduledLabel?.let {
                    InfoRow(Icons.Outlined.Schedule, stringResource(R.string.orders_date), "${stringResource(R.string.orders_date)}: $it")
                }
                InfoRow(
                    icon = Icons.Outlined.Place,
                    contentDescription = stringResource(R.string.orders_address),
                    text = if (canShowPrivateDetails) {
                        "${stringResource(R.string.orders_address)}: ${order.addressLabel}"
                    } else {
                        "La dirección exacta estará disponible cuando aceptes el pedido."
                    },
                )
                val service = listOf(order.rubroLabel, order.subRubroLabel).filter { it.isNotBlank() }.joinToString(" · ")
                if (service.isNotBlank()) {
                    InfoRow(Icons.Outlined.HomeRepairService, stringResource(R.string.orders_service), service)
                }
                if (canShowPrivateDetails && order.contactName.isNotBlank()) {
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
        val context = LocalContext.current
        if (canShowPrivateDetails && addressLatitude != null && addressLongitude != null) {
            Column(
                modifier = Modifier.clickable {
                    openOrderMap(context, addressLatitude, addressLongitude, order.addressLabel)
                },
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
            ) {
                Text(
                    text = stringResource(
                        if (state.tracking != null && !isWorker) {
                            R.string.orders_map_worker_location
                        } else {
                            R.string.orders_map_service_location
                        },
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                OficiosTrackingMap(
                    destinationLatitude = addressLatitude,
                    destinationLongitude = addressLongitude,
                    workerLatitude = if (!isWorker) state.tracking?.location?.latitude else null,
                    workerLongitude = if (!isWorker) state.tracking?.location?.longitude else null,
                    onMapClick = { openOrderMap(context, addressLatitude, addressLongitude, order.addressLabel) },
                    mapContentDescription = stringResource(R.string.orders_open_map),
                )
                if (!isWorker && order.bookingStatus == BookingStatus.EN_ROUTE && state.tracking == null) {
                    InfoText(stringResource(R.string.orders_tracking_waiting))
                }
            }
        }

        when {
            order.status == ServiceRequestStatus.PENDING && isWorker -> {
                Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm), modifier = Modifier.fillMaxWidth()) {
                    OficiosButton(
                        text = "Rechazar",
                        onClick = { showRejectDialog = true },
                        modifier = Modifier.weight(1f),
                        variant = OficiosButtonVariant.Danger,
                        enabled = !state.isActionInProgress,
                    )
                    OficiosButton(
                        text = "Aceptar",
                        onClick = { onEvent(OrderDetailEvent.Accept) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isActionInProgress,
                    )
                }
                OficiosButton(
                    text = "Reagendar",
                    onClick = { onEvent(OrderDetailEvent.OpenCounterOffer) },
                    variant = OficiosButtonVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isActionInProgress,
                )
            }

            order.status == ServiceRequestStatus.PENDING && !isWorker -> {
                InfoText("Esperando respuesta del trabajador")
                CancelOrderButton(enabled = !state.isActionInProgress, onClick = { showCancelDialog = true })
            }

            order.status == ServiceRequestStatus.COUNTER_OFFER && order.pendingCounterOffer != null -> {
                order.pendingCounterOffer?.let { offer ->
                    OficiosCard {
                        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                            Text(
                                text = "El trabajador propuso reagendar",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(text = "Nueva fecha: ${offer.proposedLabel}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            offer.proposedPrice?.let {
                                Text(text = "Nuevo precio: $$it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                if (!isWorker) {
                    Row(horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm), modifier = Modifier.fillMaxWidth()) {
                        OficiosButton(
                            text = "Rechazar",
                            onClick = { onEvent(OrderDetailEvent.RejectCounterOffer) },
                            modifier = Modifier.weight(1f),
                            variant = OficiosButtonVariant.Danger,
                            enabled = !state.isActionInProgress,
                        )
                        OficiosButton(
                            text = "Aceptar",
                            onClick = { onEvent(OrderDetailEvent.AcceptCounterOffer) },
                            modifier = Modifier.weight(1f),
                            enabled = !state.isActionInProgress,
                        )
                    }
                } else {
                    InfoText("Esperando respuesta del cliente a tu contrapropuesta")
                }
            }

            // pendingCounterOffer nulo con status COUNTER_OFFER sería un dato inconsistente
            // del backend (la contrapropuesta debería venir siempre en ese estado) — igual se
            // cubre para no dejar la pantalla en blanco si llega a pasar.
            order.status == ServiceRequestStatus.COUNTER_OFFER -> {
                InfoText("No pudimos cargar el detalle de la contrapropuesta. Volvé a intentar más tarde.")
            }

            order.status == ServiceRequestStatus.ACCEPTED -> when (order.bookingStatus) {
                BookingStatus.CONFIRMED -> {
                    InfoText("Pedido confirmado")
                    if (isWorker) {
                        OficiosButton(
                            text = "Estoy en camino",
                            onClick = { onEvent(OrderDetailEvent.MarkEnRoute) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isActionInProgress,
                        )
                    }
                }
                BookingStatus.EN_ROUTE -> {
                    InfoText(if (isWorker) "Marcaste que estás en camino" else "El trabajador está en camino")
                    if (isWorker) {
                        OficiosButton(
                            text = "Marcar llegada",
                            onClick = { onEvent(OrderDetailEvent.MarkArrived) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isActionInProgress,
                        )
                    }
                }
                BookingStatus.IN_PROGRESS -> {
                    InfoText("Servicio en curso")
                    if (!isWorker) {
                        CompletionCodeCard(
                            code = order.completionCode,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        OficiosButton(
                            text = "Terminar pedido",
                            onClick = { showFinalizeDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isActionInProgress,
                        )
                    }
                }
                BookingStatus.FINISHED -> {
                    InfoText("Pedido finalizado")
                    if (!isWorker) {
                        OficiosButton(
                            text = "Puntuar al trabajador",
                            onClick = { onEvent(OrderDetailEvent.GoToRate) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                // bookingStatus fuera de lo esperado para un pedido ACCEPTED (ej. PENDING o
                // CANCELLED) sería inconsistente, pero se muestra algo accionable en vez de
                // dejar la pantalla en blanco.
                else -> {
                    InfoText("No pudimos determinar el estado actual de este pedido.")
                    OficiosButton(
                        text = "Reintentar",
                        onClick = { onEvent(OrderDetailEvent.Retry) },
                        variant = OficiosButtonVariant.Outlined,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            order.status == ServiceRequestStatus.CANCELLED -> {
                InfoText(order.rejectionReason?.let { "Rechazado: $it" } ?: "Pedido cancelado")
            }

            order.status == ServiceRequestStatus.EXPIRED -> {
                InfoText("La solicitud expiró")
            }

            else -> {
                InfoText("No pudimos determinar el estado de este pedido, contactá soporte.")
            }
        }

        if (order.canCancelBooking && order.status != ServiceRequestStatus.PENDING && order.status != ServiceRequestStatus.CANCELLED) {
            CancelOrderButton(enabled = !state.isActionInProgress, onClick = { showCancelDialog = true })
        }
    }

    if (showRejectDialog) {
        ReasonDialog(
            title = "Rechazar solicitud",
            confirmLabel = "Rechazar",
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
            actionSize = OficiosButtonSize.Prominent,
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

    if (showFinalizeDialog) {
        FinalizeCodeDialog(
            onConfirm = { code ->
                onEvent(OrderDetailEvent.FinalizeWithCode(code))
                showFinalizeDialog = false
            },
            onDismiss = { showFinalizeDialog = false },
        )
    }
}

private enum class TimelinePhase { Done, Current, Upcoming }

@Composable
private fun OrderTimeline(status: ServiceRequestStatus, bookingStatus: BookingStatus?) {
    val steps = orderTimeline(status, bookingStatus)
    if (steps.isEmpty()) return
    val active = orderTimelineActiveIndex(status, bookingStatus)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.Top,
    ) {
        steps.forEachIndexed { index, step ->
            val phase = when {
                index < active -> TimelinePhase.Done
                index == active -> TimelinePhase.Current
                else -> TimelinePhase.Upcoming
            }
            if (index > 0) {
                TimelineConnector(done = index <= active)
            }
            TimelineStep(step = step, phase = phase)
        }
    }
}

@Composable
private fun TimelineConnector(done: Boolean) {
    Box(
        modifier = Modifier
            .padding(top = (OficiosComponentSize.iconMd - 2.dp) / 2)
            .width(OficiosSpacing.md)
            .height(2.dp)
            .background(if (done) OficiosColorWhite else OficiosColorWhite.copy(alpha = 0.4f)),
    )
}

@Composable
private fun TimelineStep(step: OrderTimelineStep, phase: TimelinePhase) {
    val label = when (step) {
        OrderTimelineStep.REQUESTED -> stringResource(R.string.orders_step_requested)
        OrderTimelineStep.CONFIRMED -> stringResource(R.string.orders_step_confirmed)
        OrderTimelineStep.EN_ROUTE -> stringResource(R.string.orders_step_en_route)
        OrderTimelineStep.IN_PROGRESS -> stringResource(R.string.orders_step_in_progress)
        OrderTimelineStep.FINISHED -> stringResource(R.string.orders_step_finished)
    }
    val description = stringResource(
        when (phase) {
            TimelinePhase.Done -> R.string.orders_step_done
            TimelinePhase.Current -> R.string.orders_step_current
            TimelinePhase.Upcoming -> R.string.orders_step_upcoming
        },
        label,
    )
    val circle = when (phase) {
        TimelinePhase.Done -> OficiosColorAction
        TimelinePhase.Current -> OficiosColorAccent
        TimelinePhase.Upcoming -> OficiosColorOutlineFill
    }
    val iconTint = when (phase) {
        TimelinePhase.Done -> OficiosColorWhite
        TimelinePhase.Current -> OficiosColorBackground
        TimelinePhase.Upcoming -> OficiosColorWhite.copy(alpha = 0.72f)
    }
    val labelColor = if (phase == TimelinePhase.Upcoming) {
        OficiosColorWhite.copy(alpha = 0.72f)
    } else {
        OficiosColorWhite
    }
    Column(
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
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
                imageVector = orderTimelineStepIcon(step),
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
            softWrap = false,
        )
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
private fun CancelOrderButton(enabled: Boolean, onClick: () -> Unit) {
    OficiosButton(
        text = stringResource(R.string.orders_cancel),
        onClick = onClick,
        leadingIcon = Icons.Outlined.Close,
        variant = OficiosButtonVariant.OutlinedDanger,
        size = OficiosButtonSize.Prominent,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
    )
}

@Composable
private fun InfoText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun InfoRow(icon: ImageVector, contentDescription: String, text: String) {
    Row(
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
private fun CompletionCodeCard(
    code: String?,
    modifier: Modifier = Modifier,
) {
    OficiosCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(OficiosSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
        ) {
            Text(
                text = "Código de finalización",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (code.isNullOrBlank()) {
                Text(
                    text = "Tu código aparecerá acá cuando el servicio esté en curso. Compartilo con el trabajador al finalizar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = code,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Mostrá este código al trabajador para confirmar que el servicio se realizó.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FinalizeCodeDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    OficiosDialog(
        title = "Terminar pedido",
        message = "Pedile al cliente el código de finalización e ingresalo para cerrar el servicio.",
        confirmLabel = "Finalizar",
        dismissLabel = "Cancelar",
        confirmEnabled = code.length == 6,
        onConfirm = { onConfirm(code.trim()) },
        onDismiss = onDismiss,
        content = {
            OficiosTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it.filter { ch -> ch.isDigit() } },
                label = "Código de 6 dígitos",
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
        message = "Contanos el motivo para que la otra persona lo vea.",
        confirmLabel = confirmLabel,
        isDangerous = true,
        confirmEnabled = reason.isNotBlank(),
        onConfirm = { onConfirm(reason) },
        onDismiss = onDismiss,
        content = {
            OficiosTextField(
                value = reason,
                onValueChange = { reason = it },
                label = "Motivo",
            )
        },
    )
}
