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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Componente de entrada de código de dígitos según Figma 382:110
 * 
 * Muestra casillas individuales para cada dígito del código de verificación.
 * El código completo se maneja como un String único.
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
    val focusRequester = remember { FocusRequester() }
    
    // Auto-focus después del primer frame para evitar IllegalStateException
    LaunchedEffect(Unit) {
        withFrameNanos {}
        focusRequester.requestFocus()
    }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.xs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Campo de texto invisible que maneja la entrada
        BasicTextField(
            value = TextFieldValue(
                text = value.take(digitCount),
                selection = TextRange(value.length.coerceAtMost(digitCount))
            ),
            onValueChange = { newValue ->
                val filtered = newValue.text.filter { it.isDigit() }.take(digitCount)
                onValueChange(filtered)
                if (filtered.length == digitCount) {
                    onDone()
                }
            },
            modifier = Modifier
                .size(0.dp)
                .focusRequester(focusRequester),
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            cursorBrush = SolidColor(Color.Transparent),
        )
        
        // Mostrar las casillas visuales
        repeat(digitCount) { index ->
            CodeDigitBox(
                digit = value.getOrNull(index)?.toString() ?: "",
                isError = isError,
                isFilled = index < value.length,
            )
        }
    }
}

@Composable
private fun CodeDigitBox(
    digit: String,
    isError: Boolean,
    isFilled: Boolean,
) {
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
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
                width = 1.dp,
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
