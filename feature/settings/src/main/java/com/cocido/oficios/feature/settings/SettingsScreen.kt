package com.cocido.oficios.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.oficios.core.designsystem.component.AppCard
import com.cocido.oficios.core.designsystem.component.AppScaffold
import com.cocido.oficios.core.designsystem.component.AppTopBar
import com.cocido.oficios.core.designsystem.theme.AppDimens
import com.cocido.oficios.core.designsystem.theme.AppSpacing
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_title),
                navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                onNavigationClick = { onEvent(SettingsEvent.NavigateBack) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            AppCard(modifier = Modifier.widthIn(max = AppDimens.contentMaxWidth)) {
                Text(
                    text = stringResource(R.string.settings_theme_section),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Column(
                    modifier = Modifier
                        .padding(top = AppSpacing.md)
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
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
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
