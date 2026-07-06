package com.cocido.oficios.domain.model

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data class Authenticated(val session: UserSession) : AuthState
}
