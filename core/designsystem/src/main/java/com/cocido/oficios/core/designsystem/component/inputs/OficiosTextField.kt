package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
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

private val TrailingIconContentInset = 48.dp

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
    // M3 oculta el placeholder en cuanto hay texto: sin label el campo queda sin nombre visible.
    val fieldLabel = label.ifBlank { placeholder.orEmpty() }
    val fieldPlaceholder = placeholder?.takeIf { label.isNotBlank() && it != label }
    val parentBringIntoView = LocalFieldAndCtaBringIntoView.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var fieldSize by remember { mutableStateOf(IntSize.Zero) }
    var fieldCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val ctaClearancePx = with(density) {
        (OficiosComponentSize.buttonHeightLarge + OficiosSpacing.md + OficiosComponentSize.buttonHeightSmall).toPx()
    }
    val fieldTextStyle = MaterialTheme.typography.bodyLarge
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val resolvedMaxLines = if (singleLine) 1 else maxLines.coerceAtLeast(minLines.coerceAtLeast(1))
    val showValueEllipsis = !isFocused && value.isNotEmpty()
    val displayedValue = remember(value, visualTransformation) {
        visualTransformation.filter(AnnotatedString(value)).text
    }
    val valueColor = when {
        !enabled -> OficiosColorMutedText
        isError -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    val containerModifier = Modifier
        .fillMaxWidth()
        .heightIn(min = OficiosComponentSize.textFieldHeight)
        .onSizeChanged { fieldSize = it }
        .onGloballyPositioned { fieldCoordinates = it }
        .then(
            if (parentBringIntoView == null) {
                Modifier.bringIntoViewRequester(bringIntoViewRequester)
            } else {
                Modifier
            },
        )
    val textFieldModifier = modifier
        .fillMaxWidth()
        .onFocusChanged { focusState ->
            if (parentBringIntoView != null) {
                parentBringIntoView.onFieldFocus(focusState.isFocused, fieldCoordinates)
            } else if (focusState.isFocused && fieldSize != IntSize.Zero) {
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
        }
    val contentPadding = if (fieldLabel.isNotBlank()) {
        TextFieldDefaults.contentPaddingWithLabel()
    } else {
        TextFieldDefaults.contentPaddingWithoutLabel()
    }
    val trailingInset = if (trailingContent != null) TrailingIconContentInset else 0.dp
    // Sin altura fija: con label, M3 suma 8dp arriba de la caja de 56dp y el supportingText va debajo.
    Box(modifier = containerModifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = textFieldModifier,
            label = if (fieldLabel.isNotBlank()) {
                { Text(text = fieldLabel, style = MaterialTheme.typography.bodyMedium) }
            } else {
                null
            },
            placeholder = fieldPlaceholder?.let { { Text(text = it, style = MaterialTheme.typography.bodyLarge, color = placeholderColor) } },
            enabled = enabled,
            readOnly = readOnly,
            isError = isError,
            supportingText = supportingText?.let { { Text(text = it, style = MaterialTheme.typography.labelSmall) } },
            textStyle = fieldTextStyle,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else minLines.coerceAtLeast(1),
            maxLines = resolvedMaxLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = OficiosAlpha.disabled),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = if (showValueEllipsis) Color.Transparent else MaterialTheme.colorScheme.onSurface,
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
        if (showValueEllipsis) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(contentPadding)
                    .padding(end = trailingInset),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = displayedValue,
                    style = fieldTextStyle,
                    color = valueColor,
                    maxLines = resolvedMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
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
