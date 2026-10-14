package com.cocido.oficios.feature.auth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.component.brand.OficiosBrandBlock
import com.cocido.oficios.core.designsystem.component.brand.OficiosBrandScale
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
    support: @Composable (ColumnScope.() -> Unit)? = null,
    footer: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    // El IME levanta todo el bloque. La barra de gestos entra en la altura medida
    // del CTA anclado, y ese valor es el padding inferior del contenido.
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val density = LocalDensity.current
    var anchoredHeight by remember {
        mutableStateOf(OficiosComponentSize.buttonHeightLarge + OficiosSpacing.lg)
    }
    OficiosScaffold(
        modifier = modifier.fillMaxSize(),
        isLoading = isLoading,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .consumeWindowInsets(padding)
                .padding(horizontal = OficiosSpacing.md)
                .padding(bottom = imeBottom),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = contentTopPadding,
                        bottom = if (footer != null) anchoredHeight else OficiosSpacing.md,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(OficiosSpacing.xl),
            ) {
                if (showBrand) {
                    OficiosBrandBlock(
                        modifier = Modifier.widthIn(max = OficiosComponentSize.authBrandMaxWidth),
                        scale = brandScale,
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                    content = content,
                )

                if (support != null && footer == null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        content = support,
                    )
                }
            }

            if (footer != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            anchoredHeight = with(density) { size.height.toDp() }
                        }
                        .padding(
                            bottom = OficiosSpacing.lg +
                                if (imeBottom > 0.dp) 0.dp else navigationBottom,
                        ),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                ) {
                    if (support != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            content = support,
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                        content = footer,
                    )
                }
            }
        }
    }
}
