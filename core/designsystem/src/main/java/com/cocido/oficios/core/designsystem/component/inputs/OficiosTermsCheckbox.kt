package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.theme.OficiosAlpha
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

@Composable
fun OficiosTermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    prefix: String,
    termsLabel: String,
    compactRow: Boolean = true,
) {
    val labelColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = OficiosAlpha.disabled)
    }

    Row(
        modifier = modifier.clickable(enabled = enabled) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Box(
            modifier = if (compactRow) {
                Modifier
                    .padding(start = OficiosSpacing.xxs)
                    .padding(
                        end = OficiosComponentSize.checkboxLabelGap,
                        top = OficiosComponentSize.checkboxRowPaddingVertical,
                        bottom = OficiosComponentSize.checkboxRowPaddingVertical,
                    )
            } else {
                Modifier
            },
            contentAlignment = Alignment.Center,
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier.size(OficiosComponentSize.checkboxSize),
                enabled = enabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                    disabledCheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = OficiosAlpha.disabled),
                    disabledUncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = OficiosAlpha.disabled),
                ),
            )
        }

        val annotatedLabel = buildAnnotatedString {
            val normalizedPrefix = prefix.trimEnd()
            append(normalizedPrefix)
            append('\u00A0')
            pushStringAnnotation(tag = "terms", annotation = "terms")
            withStyle(
                SpanStyle(
                    color = labelColor,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                append(termsLabel)
            }
            pop()
        }

        ClickableText(
            text = annotatedLabel,
            modifier = Modifier
                .weight(1f, fill = true)
                .padding(end = OficiosSpacing.xs),
            style = MaterialTheme.typography.bodyLarge.copy(color = labelColor),
            onClick = { offset ->
                annotatedLabel.getStringAnnotations(tag = "terms", start = offset, end = offset)
                    .firstOrNull()
                    ?.let { onTermsClick() }
                    ?: onCheckedChange(!checked)
            },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OficiosTermsCheckboxPreview() {
    OficiosTheme {
        OficiosTermsCheckbox(
            checked = true,
            onCheckedChange = {},
            onTermsClick = {},
            prefix = "Acepto los ",
            termsLabel = "términos y condiciones",
        )
    }
}
