package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Componente de entrada de código de dígitos según Figma 382:110
 * 
 * Usa un BasicTextField real con decorationBox que dibuja las casillas individuales.
 * Este patrón evita race conditions de focus/bring-into-view con campos ocultos de 0dp.
 * 
 * @param digitCount Cantidad de dígitos (6 para email, variable para teléfono según API)
 */
@Composable
fun OficiosCodeInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    digitCount: Int = 6,
    isError: Boolean = false,
    enabled: Boolean = true,
    onDone: () -> Unit = {},
) {
    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() }.take(digitCount)
            onValueChange(filtered)
            if (filtered.length == digitCount) {
                onDone()
            }
        },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(digitCount) { index ->
                    CodeDigitBox(
                        digit = value.getOrNull(index)?.toString() ?: "",
                        isError = isError,
                        isFilled = index < value.length,
                        isFocused = index == value.length,
                    )
                }
            }
            // Campo de texto invisible para mantener cursor/edición
            Box(modifier = Modifier.size(0.dp)) {
                innerTextField()
            }
        },
    )
}

@Composable
private fun CodeDigitBox(
    digit: String,
    isError: Boolean,
    isFilled: Boolean,
    isFocused: Boolean = false,
) {
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        isFilled -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Code Input Empty", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun PreviewCodeInputEmpty() {
    OficiosTheme {
        OficiosCodeInput(
            value = "",
            onValueChange = {},
        )
    }
}

@Preview(name = "Code Input Partial", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun PreviewCodeInputPartial() {
    OficiosTheme {
        OficiosCodeInput(
            value = "123",
            onValueChange = {},
        )
    }
}

@Preview(name = "Code Input Complete", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun PreviewCodeInputComplete() {
    OficiosTheme {
        OficiosCodeInput(
            value = "123456",
            onValueChange = {},
        )
    }
}

@Preview(name = "Code Input Error", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun PreviewCodeInputError() {
    OficiosTheme {
        OficiosCodeInput(
            value = "123456",
            onValueChange = {},
            isError = true,
        )
    }
}
