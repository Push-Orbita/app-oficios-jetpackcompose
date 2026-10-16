package com.cocido.oficios.feature.auth.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AuthRegisterLayout(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    footer: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AuthScreenLayout(
        modifier = modifier,
        isLoading = isLoading,
        showBrand = false,
        horizontalAlignment = Alignment.Start,
        footer = footer,
        content = content,
    )
}
