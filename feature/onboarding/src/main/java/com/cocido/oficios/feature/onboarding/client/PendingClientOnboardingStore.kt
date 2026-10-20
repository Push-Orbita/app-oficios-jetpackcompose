package com.cocido.oficios.feature.onboarding.client

import com.cocido.oficios.domain.model.ClientDomicilioRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingClientOnboardingStore @Inject constructor() {
    var pendingDomicilio: ClientDomicilioRequest? = null
}
