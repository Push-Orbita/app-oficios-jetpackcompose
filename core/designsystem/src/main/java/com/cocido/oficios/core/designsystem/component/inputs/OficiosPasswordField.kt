package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

@Composable
fun OficiosPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    floatingLabel: Boolean = true,
    hiddenVisibilityIconRes: Int = OficiosIcons.Eye,
    visibleVisibilityIconRes: Int = OficiosIcons.EyeClosed,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OficiosTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier.fillMaxWidth(),
        placeholder = placeholder,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = keyboardOptions.copy(keyboardType = KeyboardType.Password),
        keyboardActions = keyboardActions,
        floatingLabel = floatingLabel,
        trailingContent = {
            Box(
                modifier = Modifier.size(OficiosComponentSize.iconMd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        if (passwordVisible) visibleVisibilityIconRes else hiddenVisibilityIconRes,
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(OficiosComponentSize.iconMd),
                )
                Box(
                    modifier = Modifier
                        .requiredSize(OficiosComponentSize.minTouchTarget)
                        .clickable(enabled = enabled) { passwordVisible = !passwordVisible }
                        .semantics(mergeDescendants = true) {
                            role = Role.Button
                            this.contentDescription = if (passwordVisible) {
                                "Ocultar contraseña"
                            } else {
                                "Mostrar contraseña"
                            }
                        },
                )
            }
        },
    )
}

@Preview(name = "Password Field", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun PasswordFieldPreview() {
    OficiosTheme {
        Column {
            OficiosPasswordField(value = "", onValueChange = {}, label = "Contraseña", placeholder = "Contraseña")
            OficiosPasswordField(value = "secret", onValueChange = {}, label = "Repetir contraseña")
            OficiosPasswordField(
                value = "12345",
                onValueChange = {},
                label = "Contraseña",
                isError = true,
                supportingText = "Las contraseñas no coinciden"
            )
        }
    }
}
