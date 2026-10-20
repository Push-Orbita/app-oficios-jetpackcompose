package com.cocido.oficios.feature.home.rubros

import androidx.compose.runtime.Composable
import com.cocido.oficios.core.designsystem.component.feedback.LocalOficiosSnackbarController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.feature.home.R

@Composable
fun RubrosRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSearchResults: (Long, String, Long, String, String?) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RubrosViewModel = hiltViewModel(),
) {
    val comingSoon = stringResource(R.string.home_coming_soon)
    val snackbar = LocalOficiosSnackbarController.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RubrosEffect.NavigateBack -> onNavigateBack()
                is RubrosEffect.NavigateToSearchResults -> onNavigateToSearchResults(
                    effect.rubroId,
                    effect.rubroLabel,
                    effect.categoryId,
                    effect.categoryLabel,
                    effect.query,
                )
                RubrosEffect.ShowComingSoon -> onShowMessage(comingSoon)
                is RubrosEffect.ShowMessage -> snackbar.showError(effect.message)
            }
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RubrosScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}
