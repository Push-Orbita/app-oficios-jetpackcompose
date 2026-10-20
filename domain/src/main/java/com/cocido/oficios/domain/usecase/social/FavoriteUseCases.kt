package com.cocido.oficios.domain.usecase.social

import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.FavoritePage
import com.cocido.oficios.domain.model.FavoriteStatus
import com.cocido.oficios.domain.repository.FavoritesRepository
import java.util.UUID
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(pageNumber: Int = 1, pageSize: Int = 20): AppResult<FavoritePage> =
        favoritesRepository.getFavorites(pageNumber, pageSize)
}

class GetFavoriteStatusUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(workerId: Long): AppResult<FavoriteStatus> =
        favoritesRepository.getStatus(workerId)
}

class ToggleFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(workerId: Long, currentlyFavorite: Boolean): AppResult<Unit> {
        return if (currentlyFavorite) {
            favoritesRepository.remove(workerId)
        } else {
            favoritesRepository.add(workerId, UUID.randomUUID().toString())
        }
    }
}
