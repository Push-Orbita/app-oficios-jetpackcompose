package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosColorErrorBorder
import com.cocido.oficios.core.designsystem.theme.OficiosColorMutedText
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// OficiosTextField
//
// Figma (Frame 938 / inputs de formulario): label ARRIBA del campo,
// gap 10dp, caja 48dp. Auth usa floatingLabel=false (solo placeholder).
//
// Estados visuales:
//   default  → border outline
//   focused  → border primary
//   error    → border error
//   disabled → alpha reducido
//   readonly → sin interacción
// ---------------------------------------------------------------------------

private val FieldBorderWidth = 1.dp

/** Visible en tests para comparar bounds del label con el borde del campo. */
internal const val OFICIOS_TEXT_FIELD_BORDER_TEST_TAG = "oficios_text_field_border"

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
    floatingLabel: Boolean = true,
    onFocusChange: (Boolean) -> Unit = {},
) {
    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = OficiosAlpha.medium)
    val fieldLabel = if (floatingLabel) label else ""
    val fieldPlaceholder = if (floatingLabel) {
        placeholder?.takeIf { it.isNotBlank() }
    } else {
        placeholder ?: label.takeIf { it.isNotBlank() }
    }
    val parentBringIntoView = LocalFieldAndCtaBringIntoView.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var fieldSize by remember { mutableStateOf(IntSize.Zero) }
    var fieldCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val ctaClearancePx = with(density) {
        (OficiosComponentSize.buttonHeightLarge + OficiosSpacing.md + OficiosComponentSize.buttonHeightSmall).toPx()
    }

    val fontScale = density.fontScale.coerceAtLeast(1f)
    fun scaled(dp: Dp): Dp = (dp.value * fontScale).dp

    val fieldTextStyle = MaterialTheme.typography.bodyLarge
    val labelTextStyle = MaterialTheme.typography.bodyLarge
    val horizontalPadding = OficiosSpacing.md
    val verticalPadding = scaled(OficiosSpacing.xs)
    val labelFieldGap = OficiosSpacing.s10

    val resolvedMinLines = if (singleLine) 1 else minLines.coerceAtLeast(1)
    val resolvedMaxLines = if (singleLine) 1 else maxLines.coerceAtLeast(resolvedMinLines)

    val valueLineHeightDp = with(density) { fieldTextStyle.lineHeight.toDp() }

    val minFieldHeight = with(density) {
        val scaledBase = OficiosComponentSize.textFieldHeight.toPx() * fontScale
        val valueHeight = if (singleLine) {
            fieldTextStyle.lineHeight.toPx()
        } else {
            fieldTextStyle.lineHeight.toPx() * resolvedMinLines
        }
        val verticalInsets = verticalPadding.toPx() * 2
        maxOf(scaledBase, valueHeight + verticalInsets).toDp()
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = OficiosAlpha.disabled)
        isError -> OficiosColorErrorBorder
        isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    val labelColor = when {
        !enabled -> OficiosColorMutedText
        isError -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    val valueColor = when {
        !enabled -> OficiosColorMutedText
        else -> MaterialTheme.colorScheme.onSurface
    }
    val containerColor = if (!enabled) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        Color.Transparent
    }

    val showValueEllipsis = value.isNotEmpty() && (readOnly || !isFocused)
    val displayedValue = remember(value, visualTransformation) {
        visualTransformation.filter(AnnotatedString(value)).text
    }

    val lockAuthFieldHeight = fieldLabel.isBlank() && singleLine && fontScale <= 1.15f
    val fieldModifier = Modifier
        .fillMaxWidth()
        .then(
            if (lockAuthFieldHeight) {
                Modifier.height(OficiosComponentSize.textFieldHeight)
            } else {
                Modifier.heightIn(min = minFieldHeight)
            },
        )
        .onSizeChanged { fieldSize = it }
        .onGloballyPositioned { fieldCoordinates = it }
        .then(
            if (parentBringIntoView == null) {
                Modifier.bringIntoViewRequester(bringIntoViewRequester)
            } else {
                Modifier
            },
        )
        .semantics {
            val a11yLabel = fieldLabel.ifBlank { label.ifBlank { placeholder.orEmpty() } }
            if (a11yLabel.isNotBlank()) {
                contentDescription = a11yLabel
            }
            if (supportingText != null) {
                stateDescription = supportingText
            }
        }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (fieldLabel.isNotBlank()) {
            Text(
                text = fieldLabel,
                style = labelTextStyle,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = labelFieldGap),
            )
        }
        Box(modifier = fieldModifier) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .testTag(OFICIOS_TEXT_FIELD_BORDER_TEST_TAG)
                    .background(containerColor, MaterialTheme.shapes.small)
                    .border(BorderStroke(FieldBorderWidth, borderColor), MaterialTheme.shapes.small),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(
                            start = horizontalPadding,
                            end = horizontalPadding,
                            top = verticalPadding,
                            bottom = verticalPadding,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs),
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(OficiosComponentSize.iconMd),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = valueLineHeightDp),
                    ) {
                        if (value.isEmpty() && fieldPlaceholder != null) {
                            Text(
                                text = fieldPlaceholder,
                                style = fieldTextStyle,
                                color = placeholderColor,
                                maxLines = resolvedMaxLines,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (showValueEllipsis) {
                            Text(
                                text = displayedValue,
                                style = fieldTextStyle,
                                color = valueColor,
                                maxLines = resolvedMaxLines,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(modifier)
                                .then(
                                    if (showValueEllipsis && singleLine) {
                                        Modifier.heightIn(max = valueLineHeightDp)
                                    } else {
                                        Modifier
                                    },
                                )
                                .onFocusChanged { focusState ->
                                    onFocusChange(focusState.isFocused)
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
                                },
                            enabled = enabled,
                            readOnly = readOnly,
                            textStyle = fieldTextStyle.copy(
                                color = if (showValueEllipsis) Color.Transparent else valueColor,
                            ),
                            keyboardOptions = keyboardOptions,
                            keyboardActions = keyboardActions,
                            singleLine = singleLine,
                            minLines = resolvedMinLines,
                            maxLines = resolvedMaxLines,
                            visualTransformation = visualTransformation,
                            interactionSource = interactionSource,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        )
                    }
                    if (trailingContent != null) {
                        trailingContent()
                    }
                }
            }
        }
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(
                    start = horizontalPadding,
                    top = OficiosSpacing.xxs,
                ),
            )
        }
    }
}

@Preview(name = "TextFields States", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun TextFieldsPreview() {
    OficiosTheme {
        Column(verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md)) {
            OficiosTextField(value = "", onValueChange = {}, label = "Barrio", placeholder = "Busque y seleccione el barrio")
            OficiosTextField(value = "", onValueChange = {}, label = "Calle")
            OficiosTextField(value = "usuario@mail.com", onValueChange = {}, label = "Email")
            OficiosTextField(value = "mal@", onValueChange = {}, label = "Email", isError = true, supportingText = "Email inválido")
            OficiosTextField(value = "", onValueChange = {}, label = "Deshabilitado", enabled = false)
        }
    }
}
