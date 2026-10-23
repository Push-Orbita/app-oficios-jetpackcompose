package com.cocido.oficios.feature.settings.configuration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.core.designsystem.theme.selectedContent
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.feature.settings.R

@Composable
fun ConfigurationScreen(
    uiState: ConfigurationUiState,
    onEvent: (ConfigurationEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.settings_configuration),
                onBack = { onEvent(ConfigurationEvent.NavigateBack) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(OficiosSpacing.OficiosScreenPaddingAfterTopBar),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.settings_theme_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = OficiosComponentSize.contentMaxWidth),
            )
            ThemeModeCard(
                selected = uiState.themeMode,
                onSelect = { onEvent(ConfigurationEvent.SetThemeMode(it)) },
            )
        }
    }
}

@Composable
private fun ThemeModeCard(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    val options = listOf(
        ThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
        ThemeMode.DARK to stringResource(R.string.settings_theme_dark),
        ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
    )
    OficiosCard(modifier = Modifier.fillMaxWidth().widthIn(max = OficiosComponentSize.contentMaxWidth)) {
        Column {
            options.forEach { (mode, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = OficiosComponentSize.minTouchTarget)
                        .clickable { onSelect(mode) }
                        .semantics(mergeDescendants = true) { contentDescription = label },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selected == mode,
                        onClick = { onSelect(mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                    Spacer(Modifier.width(OficiosSpacing.sm))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected == mode) {
                            OficiosTheme.colors.selectedContent(onFilledPrimary = false)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }
    }
}

@Preview(name = "Configuración — tema claro", showBackground = true)
@Composable
private fun ConfigurationScreenPreview() {
    OficiosTheme {
        ConfigurationScreen(
            uiState = ConfigurationUiState(themeMode = ThemeMode.LIGHT),
            onEvent = {},
        )
    }
}

@Preview(name = "Configuración — tema oscuro", showBackground = true)
@Composable
private fun ConfigurationScreenDarkPreview() {
    OficiosTheme(darkTheme = true) {
        ConfigurationScreen(
            uiState = ConfigurationUiState(themeMode = ThemeMode.DARK),
            onEvent = {},
        )
    }
}
