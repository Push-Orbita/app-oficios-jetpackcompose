package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

data class PasswordRequirementItem(
    val label: String,
    val satisfied: Boolean,
)

/**
 * Checklist de requisitos de contraseña. Se muestra siempre (también con el campo vacío) para
 * que el usuario sepa las reglas antes de escribir; cada ítem se marca en vivo.
 *
 * Accesibilidad: todo el bloque es un único nodo para TalkBack con un estado resumido
 * ("Faltan requisitos: …" / "La contraseña cumple los requisitos") anunciado como live region
 * cortés, así no se lee cada fila por separado en cada tecla.
 */
@Composable
fun PasswordRequirementsChecklist(
    requirements: List<PasswordRequirementItem>,
    modifier: Modifier = Modifier,
    title: String? = stringResource(R.string.ds_password_requirements_title),
    caption: String? = stringResource(R.string.ds_password_symbol_hint),
) {
    val missing = remember(requirements) {
        requirements.filterNot { it.satisfied }.map { it.label }
    }
    val summary = if (missing.isEmpty()) {
        stringResource(R.string.ds_password_requirements_all_met)
    } else {
        stringResource(
            R.string.ds_password_requirements_missing,
            missing.joinToString(", "),
        )
    }
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {
            liveRegion = LiveRegionMode.Polite
            stateDescription = summary
        },
        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xxs),
    ) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = OficiosTheme.colors.onSurfaceVariant,
            )
        }
        requirements.forEach { item ->
            PasswordRequirementRow(item = item)
        }
        if (!caption.isNullOrBlank()) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = OficiosTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PasswordRequirementRow(item: PasswordRequirementItem) {
    val state = if (item.satisfied) {
        stringResource(R.string.ds_password_requirement_met, item.label)
    } else {
        stringResource(R.string.ds_password_requirement_pending, item.label)
    }
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) {
            stateDescription = state
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(
                if (item.satisfied) OficiosIcons.Check else OficiosIcons.Circle,
            ),
            contentDescription = null,
            tint = if (item.satisfied) {
                OficiosTheme.colors.success
            } else {
                OficiosTheme.colors.disabled
            },
            modifier = Modifier.size(OficiosComponentSize.iconSm),
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodySmall,
            color = if (item.satisfied) {
                OficiosTheme.colors.onSurface
            } else {
                OficiosTheme.colors.onSurfaceVariant
            },
        )
    }
}

@Preview(name = "Password requirements", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun PasswordRequirementsChecklistPreview() {
    OficiosTheme {
        PasswordRequirementsChecklist(
            requirements = listOf(
                PasswordRequirementItem("Entre 6 y 64 caracteres", satisfied = true),
                PasswordRequirementItem("Al menos una letra", satisfied = true),
                PasswordRequirementItem("Al menos un número", satisfied = true),
                PasswordRequirementItem("Al menos un símbolo (por ej. ! @ # $ - _)", satisfied = false),
            ),
        )
    }
}
