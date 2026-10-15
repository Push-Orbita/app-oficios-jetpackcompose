package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// OficiosTextField
//
// Estados visuales (de Figma):
//   default  → border outline (#413764)
//   focused  → border primary (#A32F56)
//   error    → border error (#CF6679)
//   disabled → alpha reducido
//   readonly → sin interacción
// ---------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OficiosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
) {
    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OficiosAlpha.medium)
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var fieldSize by remember { mutableStateOf(IntSize.Zero) }
    val ctaClearancePx = with(density) {
        (OficiosComponentSize.buttonHeightLarge + OficiosSpacing.md + OficiosComponentSize.buttonHeightSmall).toPx()
    }
    // Sin altura fija: con label, M3 suma 8dp arriba de la caja de 56dp y el supportingText va debajo.
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = OficiosComponentSize.textFieldHeight)
            .onSizeChanged { fieldSize = it }
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused && fieldSize != IntSize.Zero) {
                    scope.launch {
                        bringIntoViewRequester.bringIntoView(
                            Rect(
                                left = 0f,
                                top = 0f,
                                right = fieldSize.width.toFloat(),
                                bottom = fieldSize.height + ctaClearancePx,
                            ),
                        )
                    }
                }
            },
        label = if (label.isNotBlank()) {
            { Text(text = label, style = MaterialTheme.typography.bodyMedium) }
        } else {
            null
        },
        placeholder = placeholder?.let { { Text(text = it, style = MaterialTheme.typography.bodyLarge, color = placeholderColor) } },
        enabled = enabled,
        readOnly = readOnly,
        isError = isError,
        supportingText = supportingText?.let { { Text(text = it, style = MaterialTheme.typography.labelSmall) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines.coerceAtLeast(1),
        maxLines = if (singleLine) 1 else maxLines.coerceAtLeast(minLines.coerceAtLeast(1)),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = OficiosAlpha.disabled),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            disabledTextColor = OficiosColorMutedText,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = placeholderColor,
            errorLabelColor = MaterialTheme.colorScheme.error,
            disabledLabelColor = placeholderColor.copy(alpha = OficiosAlpha.disabled),
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
        trailingIcon = trailingContent,
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "TextFields States", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun TextFieldsPreview() {
    OficiosTheme {
        Column {
            OficiosTextField(value = "", onValueChange = {}, label = "Nombre completo", placeholder = "Ej: Juan García")
            OficiosTextField(value = "usuario@mail.com", onValueChange = {}, label = "Email")
            OficiosTextField(value = "mal@", onValueChange = {}, label = "Email", isError = true, supportingText = "Email inválido")
            OficiosTextField(value = "", onValueChange = {}, label = "Deshabilitado", enabled = false)
        }
    }
}
