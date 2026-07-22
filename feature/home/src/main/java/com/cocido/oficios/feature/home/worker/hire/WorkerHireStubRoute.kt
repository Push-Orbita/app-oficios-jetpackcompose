package com.cocido.oficios.feature.home.worker.hire

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun WorkerHireStubRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerHireStubViewModel = hiltViewModel(),
) {
    WorkerHireStubScreen(
        workerId = viewModel.workerId,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

