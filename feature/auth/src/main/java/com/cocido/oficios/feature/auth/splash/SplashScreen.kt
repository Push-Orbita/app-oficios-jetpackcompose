package com.cocido.oficios.feature.auth.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.feature.auth.R
import com.cocido.oficios.core.designsystem.component.brand.OficiosBrandBlock
import com.cocido.oficios.core.designsystem.component.brand.OficiosBrandScale
import com.cocido.oficios.core.designsystem.component.feedback.OficiosErrorState
import com.cocido.oficios.core.designsystem.component.feedback.OficiosStateContent
import com.cocido.oficios.core.designsystem.component.scaffold.OficiosScaffold
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OficiosScaffold(modifier = modifier.fillMaxSize()) { padding ->
        OficiosStateContent(
            targetState = uiState,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
            contentKey = { it is SplashUiState.Error },
        ) { state ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (state) {
                    SplashUiState.Loading, is SplashUiState.Ready -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = OficiosComponentSize.authSplashLogoTop),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            OficiosBrandBlock(scale = OficiosBrandScale.Splash)
                        }
                    }
                    is SplashUiState.Error -> {
                        OficiosErrorState(
                            title = stringResource(R.string.splash_error_title),
                            description = state.message,
                            onRetry = onRetry,
                            modifier = Modifier.align(Alignment.Center),
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
