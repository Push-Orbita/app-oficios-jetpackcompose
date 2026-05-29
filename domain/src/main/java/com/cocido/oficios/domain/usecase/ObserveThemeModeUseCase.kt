package com.cocido.oficios.domain.usecase

import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveThemeModeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<ThemeMode> = userPreferencesRepository.themeMode
}
