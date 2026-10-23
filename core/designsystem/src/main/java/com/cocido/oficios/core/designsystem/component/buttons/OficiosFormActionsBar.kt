package com.cocido.oficios.core.designsystem.component.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Barra fija inferior de wizards y formularios.
 *
 * [Atrás] outlined y [Continuar]/[Guardar] filled, mismo ancho, 48 dp y el
 * mismo radio del DS. Las acciones secundarias (confirmar pin, añadir horario)
 * no van aquí: quedan junto a su contenido como [OficiosButtonVariant.Tonal].
 */
@Composable
fun OficiosFormActionsBar(
    primaryLabel: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    backLabel: String? = null,
    onBack: (() -> Unit)? = null,
    primaryEnabled: Boolean = true,
    primaryLoading: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = OficiosSpacing.md,
                    vertical = OficiosSpacing.sm,
                ),
            horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        ) {
            if (onBack != null && !backLabel.isNullOrBlank()) {
                OficiosButton(
                    text = backLabel,
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    variant = OficiosButtonVariant.Outlined,
                    size = OficiosButtonSize.Form,
                    enabled = !primaryLoading,
                )
            }
            OficiosButton(
                text = primaryLabel,
                onClick = onPrimary,
                modifier = Modifier.weight(1f),
                variant = OficiosButtonVariant.Primary,
                size = OficiosButtonSize.Form,
                enabled = primaryEnabled,
                loading = primaryLoading,
            )
        }
    }
}

/**
 * Cuerpo de formulario: contenido scrolleable + [OficiosFormActionsBar] fija abajo
 * (dentro del padding de [com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold],
 * así queda sobre el IME y la barra de gestos).
 */
@Composable
fun OficiosFormColumn(
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = OficiosSpacing.md),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(bottom = OficiosSpacing.md),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
            content = content,
        )
        footer()
    }
}

@Preview(name = "Form actions — Atrás y Continuar", showBackground = true)
@Composable
private fun FormActionsBarPreview() {
    OficiosTheme {
        OficiosFormActionsBar(
            primaryLabel = "Continuar",
            onPrimary = {},
            backLabel = "Atrás",
            onBack = {},
        )
    }
}

@Preview(name = "Form actions — Guardar", showBackground = true)
@Composable
private fun FormActionsBarPrimaryOnlyPreview() {
    OficiosTheme {
        OficiosFormActionsBar(
            primaryLabel = "Guardar",
            onPrimary = {},
        )
    }
}
