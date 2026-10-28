package com.cocido.oficios.feature.auth.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.feature.auth.R
import com.cocido.oficios.core.designsystem.component.brand.OficiosBrandBlock
import com.cocido.oficios.core.designsystem.component.brand.OficiosBrandScale
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

const val SplashBrandTestTag = "splash_brand"

@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(modifier = modifier.fillMaxSize()) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentKey = { it is SplashUiState.Error },
        ) { state ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                when (state) {
                    SplashUiState.Loading, is SplashUiState.Ready -> {
                        OficiosBrandBlock(
                            scale = OficiosBrandScale.Splash,
                            modifier = Modifier.testTag(SplashBrandTestTag),
                        )
                    }
                    is SplashUiState.Error -> {
                        OficiosErrorState(
                            title = stringResource(R.string.splash_error_title),
                            description = state.message,
                            onRetry = onRetry,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun SplashScreenPreview() {
    OficiosTheme {
        SplashScreen(uiState = SplashUiState.Loading, onRetry = {})
    }
}
