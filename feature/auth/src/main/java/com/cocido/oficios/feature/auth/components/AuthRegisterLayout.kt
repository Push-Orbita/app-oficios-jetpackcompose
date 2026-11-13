package com.cocido.oficios.feature.auth.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

@Composable
fun AuthRegisterLayout(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    contentTopPadding: Dp = OficiosComponentSize.authRegisterTitleTop,
    sectionSpacing: Dp = OficiosSpacing.md,
    topBar: @Composable (() -> Unit)? = null,
    stickyFooter: @Composable (() -> Unit)? = null,
    footer: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    AuthScreenLayout(
        modifier = modifier,
        isLoading = isLoading,
        showBrand = false,
        contentTopPadding = contentTopPadding,
        sectionSpacing = sectionSpacing,
        horizontalAlignment = Alignment.Start,
        topBar = topBar,
        stickyFooter = stickyFooter,
        footer = footer,
        content = content,
    )
}
