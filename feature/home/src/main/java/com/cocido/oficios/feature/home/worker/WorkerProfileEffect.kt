package com.cocido.oficios.feature.home.worker

sealed interface WorkerProfileEffect {
    data object NavigateBack : WorkerProfileEffect
    data class NavigateToHire(val workerId: Long) : WorkerProfileEffect
    data class ShowMessage(val message: String) : WorkerProfileEffect
    data class ShowFavoriteResult(val added: Boolean) : WorkerProfileEffect
    data class NavigateToRate(val bookingId: Long, val workerName: String) : WorkerProfileEffect
    data class NavigateToReport(val workerId: Long, val workerName: String) : WorkerProfileEffect
}

