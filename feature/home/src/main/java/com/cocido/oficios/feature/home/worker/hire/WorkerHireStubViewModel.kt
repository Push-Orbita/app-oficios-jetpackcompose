package com.cocido.oficios.feature.home.worker.hire

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WorkerHireStubViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val workerId: Long = savedStateHandle.get<Long>("workerId") ?: 0L
}

