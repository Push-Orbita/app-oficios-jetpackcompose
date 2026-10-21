package com.cocido.oficios.core.data.cache

import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientDomicilio
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientProfileStore @Inject constructor(
    coordinator: SessionCacheCoordinator,
) {
    val domicilios = CachedValue<List<ClientDomicilio>>(coordinator)
    val contactos = CachedValue<List<ClientContacto>>(coordinator)
}
