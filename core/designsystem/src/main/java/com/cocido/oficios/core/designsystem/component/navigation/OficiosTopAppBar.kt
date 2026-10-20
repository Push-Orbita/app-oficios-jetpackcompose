package com.cocido.oficios.core.designsystem.component.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosTopAppBar — barra superior estándar centrada
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OficiosTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    showDivider: Boolean = true,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    val colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
    )

    Column(modifier = modifier.fillMaxWidth()) {
        CenterAlignedTopAppBar(
            title = {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            navigationIcon = navigationIcon ?: {},
            actions = actions,
            windowInsets = windowInsets,
            colors = colors,
        )
        if (showDivider) {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

// ---------------------------------------------------------------------------
// OficiosBackTopBar — barra superior con botón de retroceso + acciones
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OficiosBackTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    showDivider: Boolean = true,
) {
    OficiosTopAppBar(
        title = title,
        subtitle = subtitle,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        actions = actions,
        showDivider = showDivider,
        modifier = modifier,
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Top App Bar", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun TopAppBarPreview() {
    OficiosTheme {
        Column {
            OficiosTopAppBar(
                title = "Oficios Formosa",
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    }
                },
            )
            OficiosBackTopBar(title = "Detalle del pedido", onBack = {}, subtitle = "Pedido #1234")
        }
    }
}
