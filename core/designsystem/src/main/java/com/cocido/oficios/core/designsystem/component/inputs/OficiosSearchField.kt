package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

// ---------------------------------------------------------------------------
// OficiosSearchField
// Basado en el buscador de Figma nodos 25:633 y 40:335
// ---------------------------------------------------------------------------

@Composable
fun OficiosSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearchClick: (() -> Unit)? = null,
    onMicClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OficiosAlpha.medium)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = keyboardActions,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = OficiosComponentSize.textFieldHeight)
            .clip(MaterialTheme.shapes.small)
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = MaterialTheme.shapes.small,
            ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OficiosSpacing.md, vertical = OficiosSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onMicClick != null) {
                    Icon(
                        imageVector = ImageVector.vectorResource(OficiosIcons.Mic),
                        contentDescription = "Buscar por voz",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(OficiosComponentSize.iconMd),
                    )
                    Spacer(Modifier.width(OficiosSpacing.xs))
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = MaterialTheme.typography.bodyLarge, color = placeholderColor)
                    }
                    innerTextField()
                }
                Spacer(Modifier.width(OficiosSpacing.md))
                if (onSearchClick != null) {
                    IconButton(onClick = onSearchClick, modifier = Modifier.size(OficiosComponentSize.iconMd)) {
                        Icon(
                            imageVector = ImageVector.vectorResource(OficiosIcons.Search),
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
    )
}

@Preview(name = "Search Field", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun SearchFieldPreview() {
    OficiosTheme {
        OficiosSearchField(
            value = "",
            onValueChange = {},
            placeholder = "¿En qué puedo ayudarte?",
            onMicClick = {},
            onSearchClick = {},
        )
    }
}
