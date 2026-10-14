package com.cocido.oficios.feature.auth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

@Composable
fun AuthRegisterLayout(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    footer: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val bottomInsets = WindowInsets.ime.union(WindowInsets.navigationBars)
    OficiosScaffold(
        modifier = modifier.fillMaxSize(),
        isLoading = isLoading,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .windowInsetsPadding(bottomInsets)
                .padding(horizontal = OficiosSpacing.xl),
            horizontalAlignment = Alignment.Start,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = OficiosSpacing.md, bottom = OficiosSpacing.md),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                content = content,
            )

            if (footer != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = OficiosSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                    content = footer,
                )
            }
        }
    }
}
