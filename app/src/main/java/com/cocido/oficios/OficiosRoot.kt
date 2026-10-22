package com.cocido.oficios

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.domain.model.isDark
import com.cocido.oficios.navigation.OficiosApp
import com.cocido.oficios.ui.MainViewModel

@Composable
fun OficiosRoot(
    initialThemeMode: ThemeMode = ThemeMode.LIGHT,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val pendingPushRoute by viewModel.pendingPushRoute.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle(initialValue = initialThemeMode)
    val darkTheme = themeMode.isDark(isSystemInDarkTheme())

    OficiosTheme(darkTheme = darkTheme) {
        OficiosApp(
            pendingPushRoute = pendingPushRoute,
            onPushRouteConsumed = viewModel::consumePushRoute,
        )
    }
}
