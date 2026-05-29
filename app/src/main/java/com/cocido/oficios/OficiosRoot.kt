package com.cocido.oficios

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.designsystem.theme.AppTheme
import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.navigation.OficiosApp
import com.cocido.oficios.ui.MainViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OficiosRoot(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    AppTheme(darkTheme = darkTheme) {
        OficiosApp()
    }
}
