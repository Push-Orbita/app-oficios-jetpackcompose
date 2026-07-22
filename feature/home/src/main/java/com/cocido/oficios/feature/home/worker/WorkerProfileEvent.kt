package com.cocido.oficios.feature.home.worker

sealed interface WorkerProfileEvent {
    data object Retry : WorkerProfileEvent
    data object Back : WorkerProfileEvent
    data object HireClicked : WorkerProfileEvent
    data object LoadNextReviews : WorkerProfileEvent
}

