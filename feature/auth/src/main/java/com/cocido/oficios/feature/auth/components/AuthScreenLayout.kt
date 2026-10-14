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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
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
    // Un solo inset inferior (teclado ∪ barra de gestos). Si el Scaffold anima la
    // barra y el contenido anima el IME por separado, el CTA sigue moviéndose
    // un instante después de que el teclado terminó de abrir.
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = contentTopPadding, bottom = OficiosSpacing.md),
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
                    modifier = Modifier
                        .widthIn(max = OficiosComponentSize.authBrandMaxWidth)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.md),
                    content = content,
                )

                if (support != null) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = OficiosComponentSize.authBrandMaxWidth)
                            .fillMaxWidth(),
                        content = support,
                    )
                }
            }

            if (footer != null) {
                Column(
                    modifier = Modifier
                        .widthIn(max = OficiosComponentSize.authBrandMaxWidth)
                        .fillMaxWidth()
                        .padding(bottom = OficiosSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(OficiosSpacing.sm),
                    content = footer,
                )
            }
        }
    }
}
