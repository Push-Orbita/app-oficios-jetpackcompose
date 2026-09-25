package com.cocido.oficios.feature.home.worker

import com.cocido.oficios.domain.model.WorkerProfile
import com.cocido.oficios.domain.model.WorkerReview

sealed interface WorkerProfileUiState {

    data object Loading : WorkerProfileUiState

    data class Error(val message: String) : WorkerProfileUiState

    data class Success(
        val profile: WorkerProfile,
        val reviews: List<WorkerReview>,
        val isLoadingReviews: Boolean = false,
        val hasMoreReviews: Boolean = false,
        val reviewsPageNumber: Int = 1,
        val totalReviewsPages: Int? = null,
        val canFavorite: Boolean = false,
        val isFavorite: Boolean = false,
        val isTogglingFavorite: Boolean = false,
        val isRefreshing: Boolean = false,
        /**
         * El perfil no trae los bookings del cliente. Calificar pide bookingId y no hay
         * forma confiable de saber si este cliente tiene uno finalizado sin calificar.
         */
        val canRate: Boolean = false,
    ) : WorkerProfileUiState
}

