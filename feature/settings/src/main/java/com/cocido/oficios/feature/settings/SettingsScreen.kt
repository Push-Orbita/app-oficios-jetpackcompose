package com.cocido.oficios.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButton
import com.cocido.oficios.core.designsystem.component.buttons.OficiosButtonVariant
import com.cocido.oficios.core.designsystem.component.cards.OficiosCard
import com.cocido.oficios.core.designsystem.component.navigation.OficiosBackTopBar
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.feature.settings.R
import androidx.compose.material3.Icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(
        modifier = modifier,
        topBar = {
            OficiosBackTopBar(
                title = stringResource(R.string.settings_title),
                onBack = { onEvent(SettingsEvent.NavigateBack) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(OficiosSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(OficiosSpacing.lg),
        ) {
            OficiosCard(modifier = Modifier.widthIn(max = OficiosComponentSize.contentMaxWidth)) {
                Text(
                    text = stringResource(R.string.settings_theme_section),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Column(
                    modifier = Modifier
                        .padding(top = OficiosSpacing.md)
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                ) {
                    ThemeModeOption(
                        label = stringResource(R.string.settings_theme_system),
                        selected = uiState.selectedThemeMode == ThemeMode.SYSTEM,
                        onSelect = { onEvent(SettingsEvent.ThemeModeChanged(ThemeMode.SYSTEM)) },
                    )
                    ThemeModeOption(
                        label = stringResource(R.string.settings_theme_light),
                        selected = uiState.selectedThemeMode == ThemeMode.LIGHT,
                        onSelect = { onEvent(SettingsEvent.ThemeModeChanged(ThemeMode.LIGHT)) },
                    )
                    ThemeModeOption(
                        label = stringResource(R.string.settings_theme_dark),
                        selected = uiState.selectedThemeMode == ThemeMode.DARK,
                        onSelect = { onEvent(SettingsEvent.ThemeModeChanged(ThemeMode.DARK)) },
                    )
                }
            }

            OficiosCard(modifier = Modifier.widthIn(max = OficiosComponentSize.contentMaxWidth)) {
                Text(
                    text = stringResource(R.string.settings_session_section),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                OficiosButton(
                    text = stringResource(R.string.settings_logout),
                    onClick = { onEvent(SettingsEvent.Logout) },
                    variant = OficiosButtonVariant.Ghost,
                    loading = uiState.isLoggingOut,
                    enabled = !uiState.isLoggingOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = OficiosSpacing.md),
                )
            }
        }
    }
}

@Composable
private fun ThemeModeOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
