package com.cocido.oficios.feature.home.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.OficiosDialog
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosLoadingState
import com.cocido.oficios.core.designsystem.component.inputs.OficiosTextField
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
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
        topBar = { OficiosBackTopBar(title = "Pedido", onBack = onNavigateBack) },
    ) { paddingValues ->
        when (uiState) {
            OrderDetailUiState.Loading -> OficiosLoadingState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                message = "Cargando pedido…",
            )
            is OrderDetailUiState.Error -> OficiosErrorState(
                title = "No pudimos cargar el pedido",
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
        OficiosCard {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                ServiceOrderStatusLabel(status = order.status, bookingStatus = order.bookingStatus)
                Text(
                    text = "${order.rubroLabel} · ${order.subRubroLabel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isWorker) "Cliente: ${order.clientName}" else "Trabajador: ${order.workerName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (order.description.isNotBlank()) {
                    Text(
                        text = order.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                order.scheduledLabel?.let {
                    Text(text = "Fecha propuesta: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = if (canShowPrivateDetails) order.addressLabel else "La dirección exacta estará disponible cuando aceptes el pedido.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (canShowPrivateDetails && order.contactName.isNotBlank()) {
                    Text(
                        text = "Contacto: ${order.contactName} · ${order.contactPhone}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        val addressLatitude = order.addressLatitude
        val addressLongitude = order.addressLongitude
        if (canShowPrivateDetails && addressLatitude != null && addressLongitude != null) {
            Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xs)) {
                Text(
                    text = if (state.tracking != null && !isWorker) "Seguimiento del trabajador" else "Ubicación del servicio",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                OficiosTrackingMap(
                    destinationLatitude = addressLatitude,
                    destinationLongitude = addressLongitude,
                    workerLatitude = if (!isWorker) state.tracking?.location?.latitude else null,
                    workerLongitude = if (!isWorker) state.tracking?.location?.longitude else null,
                )
                if (!isWorker && order.bookingStatus == BookingStatus.EN_ROUTE && state.tracking == null) {
                    InfoText("Esperando la primera actualización de ubicación del trabajador.")
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
                OficiosButton(
                    text = "Cancelar solicitud",
                    onClick = { showCancelDialog = true },
                    variant = OficiosButtonVariant.Ghost,
                    enabled = !state.isActionInProgress,
                )
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
            title = "Cancelar solicitud",
            message = "¿Seguro que querés cancelar esta solicitud?",
            confirmLabel = "Sí, cancelar",
            dismissLabel = "Volver",
            isDangerous = true,
            onConfirm = {
                onEvent(OrderDetailEvent.CancelBeforeAcceptance)
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

@Composable
private fun InfoText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
