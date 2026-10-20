package com.cocido.oficios.feature.home.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.oficios.domain.model.AuthState
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeChromeViewModel @Inject constructor(
    sessionRepository: SessionRepository,
) : ViewModel() {

    val showFavorites: StateFlow<Boolean> = sessionRepository.authState
        .map { state ->
            val role = (state as? AuthState.Authenticated)?.session?.user?.role
            role != UserRole.PROVIDER
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = true)
}
