package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Código de país para el campo de teléfono. [localExample] se muestra como hint
 * del formato esperado por el backend para ese país (número local, sin "+"/código de país).
 */
data class PhoneCountryOption(
    val dialCode: String,
    val flagEmoji: String,
    val countryName: String,
    val localExample: String,
)

object PhoneCountryOptions {
    val ARGENTINA = PhoneCountryOption(
        dialCode = "+54",
        flagEmoji = "🇦🇷",
        countryName = "Argentina",
        localExample = "9 3705219109",
    )

    // Oficios Formosa opera solo en Argentina; la lista queda abierta a
    // sumar países si la app se expande a otros mercados.
    val DEFAULT: List<PhoneCountryOption> = listOf(ARGENTINA)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OficiosPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    selectedCountry: PhoneCountryOption,
    onCountrySelected: (PhoneCountryOption) -> Unit = {},
    label: String,
    modifier: Modifier = Modifier,
    numberModifier: Modifier = Modifier,
    countryLabel: String = "País",
    countries: List<PhoneCountryOption> = PhoneCountryOptions.DEFAULT,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var expanded by remember { mutableStateOf(false) }
    val countrySelectable = countries.size > 1

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(OficiosSpacing.sm),
    ) {
        if (!countrySelectable) {
            OficiosTextField(
                value = "${selectedCountry.flagEmoji} ${selectedCountry.dialCode}",
                onValueChange = {},
                label = countryLabel,
                modifier = Modifier.width(COUNTRY_FIELD_WIDTH),
                readOnly = true,
                enabled = false,
                isError = isError,
            )
        } else {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.wrapContentWidth(),
        ) {
            OficiosTextField(
                value = "${selectedCountry.flagEmoji} ${selectedCountry.dialCode}",
                onValueChange = {},
                label = countryLabel,
                modifier = Modifier
                    .width(COUNTRY_FIELD_WIDTH)
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                readOnly = true,
                isError = isError,
                trailingContent = {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                countries.forEach { country ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${country.flagEmoji} ${country.countryName} (${country.dialCode})",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        onClick = {
                            onCountrySelected(country)
                            expanded = false
                        },
                    )
                }
            }
        }
        }
        OficiosTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = selectedCountry.localExample,
            modifier = numberModifier.weight(1f),
            isError = isError,
            supportingText = supportingText,
            keyboardOptions = keyboardOptions.copy(keyboardType = KeyboardType.Phone),
            keyboardActions = keyboardActions,
        )
    }
}

private val COUNTRY_FIELD_WIDTH = 140.dp

@Preview(name = "Phone Field", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OficiosPhoneFieldPreview() {
    OficiosTheme {
        OficiosPhoneField(
            value = "",
            onValueChange = {},
            selectedCountry = PhoneCountryOptions.ARGENTINA,
            onCountrySelected = {},
            label = "WhatsApp",
        )
    }
}
