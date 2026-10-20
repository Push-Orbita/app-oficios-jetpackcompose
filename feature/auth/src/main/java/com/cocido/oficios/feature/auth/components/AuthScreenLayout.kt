package com.cocido.oficios.feature.auth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import com.cocido.oficios.core.designsystem.component.brand.OficiosBrandBlock
import com.cocido.oficios.core.designsystem.component.brand.OficiosBrandScale
import com.cocido.oficios.core.designsystem.component.inputs.LocalScrollViewportHeightPx
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosSpacing

@Composable
fun AuthScreenLayout(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    brandScale: OficiosBrandScale = OficiosBrandScale.Compact,
    showBrand: Boolean = true,
    contentTopPadding: Dp = OficiosSpacing.md,
    sectionSpacing: Dp = OficiosSpacing.lg,
    brandToContentSpacing: Dp = sectionSpacing,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    support: @Composable (ColumnScope.() -> Unit)? = null,
    footer: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    // El scaffold no consume el IME: el único inset del teclado es imePadding() del Column.
    var viewportHeightPx by remember { mutableIntStateOf(0) }
    OficiosScaffold(
        modifier = modifier.fillMaxSize(),
        isLoading = isLoading,
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime),
    ) { padding ->
        CompositionLocalProvider(LocalScrollViewportHeightPx provides viewportHeightPx) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .imePadding()
                    .onSizeChanged { viewportHeightPx = it.height }
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = OficiosSpacing.xl)
                    .padding(top = contentTopPadding, bottom = OficiosSpacing.lg),
                horizontalAlignment = horizontalAlignment,
            ) {
                if (showBrand) {
                    OficiosBrandBlock(
                        modifier = Modifier.widthIn(max = OficiosComponentSize.authBrandMaxWidth),
                        scale = brandScale,
                    )
                    Spacer(Modifier.height(brandToContentSpacing))
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = horizontalAlignment,
                    verticalArrangement = Arrangement.spacedBy(sectionSpacing),
                    content = content,
                )

                if (footer != null) {
                    Spacer(Modifier.height(sectionSpacing))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = horizontalAlignment,
                        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
                        content = footer,
                    )
                }

                if (support != null) {
                    Spacer(Modifier.height(sectionSpacing))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        content = support,
                    )
                }
            }
        }
    }
}
