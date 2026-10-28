package com.cocido.oficios.core.designsystem.component.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonSize
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// Variantes de EmptyState específicas del dominio de Oficios
// ---------------------------------------------------------------------------
enum class OficiosEmptyVariant {
    NoOrders,
    NoWorkers,
    NoMessages,
    NoResults,
    Generic,
}

// ---------------------------------------------------------------------------
// OficiosLoadingState — indicador de carga centrado
// ---------------------------------------------------------------------------

@Composable
fun OficiosLoadingState(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            OficiosProgress()
            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// OficiosEmptyState — pantalla/sección sin datos
// ---------------------------------------------------------------------------

@Composable
fun OficiosEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconSize: Dp = 72.dp,
    ctaLabel: String? = null,
    onCta: (() -> Unit)? = null,
    variant: OficiosEmptyVariant = OficiosEmptyVariant.Generic,
) {
    val resolvedIcon = when (variant) {
        OficiosEmptyVariant.NoOrders -> Icons.Outlined.Inbox
        OficiosEmptyVariant.NoWorkers -> Icons.Outlined.SearchOff
        OficiosEmptyVariant.NoMessages -> Icons.Outlined.Inbox
        OficiosEmptyVariant.NoResults -> Icons.Outlined.SearchOff
        OficiosEmptyVariant.Generic -> icon
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(OficiosSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = resolvedIcon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(iconSize),
        )
        Spacer(Modifier.height(OficiosSpacing.md))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(OficiosSpacing.xs))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (ctaLabel != null && onCta != null) {
            Spacer(Modifier.height(OficiosSpacing.lg))
            OficiosButton(
                text = ctaLabel,
                onClick = onCta,
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Large,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// OficiosErrorState — error de red, servidor o genérico
// ---------------------------------------------------------------------------

@Composable
fun OficiosErrorState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.ErrorOutline,
    isNetworkError: Boolean = false,
    onRetry: (() -> Unit)? = null,
) {
    val resolvedIcon = if (isNetworkError) Icons.Outlined.WifiOff else icon
    val snackbar = LocalOficiosSnackbarController.current
    LaunchedEffect(description) {
        if (description.isNotBlank()) snackbar.showError(description)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(OficiosSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = resolvedIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(72.dp),
        )
        Spacer(Modifier.height(OficiosSpacing.md))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(OficiosSpacing.xs))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Spacer(Modifier.height(OficiosSpacing.lg))
            OficiosButton(
                text = "Reintentar",
                onClick = onRetry,
                leadingIcon = Icons.Outlined.Refresh,
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Large,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// OficiosSuccessState — confirmación de acción exitosa
// ---------------------------------------------------------------------------

@Composable
fun OficiosSuccessState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    ctaLabel: String? = null,
    onCta: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(OficiosSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = OficiosTheme.colors.success,
            modifier = Modifier.size(72.dp),
        )
        Spacer(Modifier.height(OficiosSpacing.md))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(OficiosSpacing.xs))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (ctaLabel != null && onCta != null) {
            Spacer(Modifier.height(OficiosSpacing.lg))
            OficiosButton(
                text = ctaLabel,
                onClick = onCta,
                variant = OficiosButtonVariant.Secondary,
                size = OficiosButtonSize.Large,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Loading State", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun LoadingPreview() {
    OficiosTheme { OficiosLoadingState(message = "Cargando pedidos…") }
}

@Preview(name = "Empty State", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun EmptyPreview() {
    OficiosTheme {
        OficiosEmptyState(
            title = "No tiene pedidos activos",
            description = "Cuando reciba un pedido aparecerá aquí.",
            variant = OficiosEmptyVariant.NoOrders,
            ctaLabel = "Explorar categorías",
            onCta = {},
        )
    }
}

@Preview(name = "Error State", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun ErrorPreview() {
    OficiosTheme {
        OficiosErrorState(
            title = "Sin conexión",
            description = "Revise su conexión a Internet e intente nuevamente.",
            isNetworkError = true,
            onRetry = {},
        )
    }
}

@Preview(name = "Success State", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun SuccessPreview() {
    OficiosTheme {
        OficiosSuccessState(
            title = "¡Pedido confirmado!",
            description = "El trabajador fue notificado y se pondrá en contacto a la brevedad.",
            ctaLabel = "Volver al inicio",
            onCta = {},
        )
    }
}
