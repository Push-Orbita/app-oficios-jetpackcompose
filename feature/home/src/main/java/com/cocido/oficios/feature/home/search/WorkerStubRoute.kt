package com.cocido.oficios.feature.home.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WorkerStubViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val workerId: Long = savedStateHandle.get<Long>("workerId") ?: 0L
}

@Composable
fun WorkerStubRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerStubViewModel = hiltViewModel(),
) {
    WorkerStubScreen(
        workerId = viewModel.workerId,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}
