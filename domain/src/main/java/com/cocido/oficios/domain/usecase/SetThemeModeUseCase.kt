package com.cocido.oficios.domain.usecase

import com.cocido.oficios.domain.model.ThemeMode
import com.cocido.oficios.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SetThemeModeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(themeMode: ThemeMode) {
        userPreferencesRepository.setThemeMode(themeMode)
    }
}
