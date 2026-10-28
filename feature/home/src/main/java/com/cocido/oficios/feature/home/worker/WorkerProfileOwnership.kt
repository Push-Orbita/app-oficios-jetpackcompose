package com.cocido.oficios.feature.home.worker

/**
 * Ficha pública (`GET /trabajador-oficio/{id}`) vs usuario actual.
 * Coincide si el id de la ficha es el `workerId` de `/me` o el `User.id`.
 */
internal fun isOwnPublicWorkerProfile(
    publicWorkerId: Long,
    currentUserId: Long?,
    currentProviderWorkerId: Long?,
): Boolean {
    if (publicWorkerId <= 0L) return false
    return publicWorkerId == currentProviderWorkerId || publicWorkerId == currentUserId
}
