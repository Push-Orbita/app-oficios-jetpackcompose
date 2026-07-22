package com.cocido.oficios.feature.onboarding.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingDeepLinkMapperTest {

    @Test
    fun `maps backend deep links to internal routes`() {
        assertEquals(
            OnboardingDestination.PROVIDER_DNI,
            OnboardingDeepLinkMapper.toInternalRoute("onboarding/identidad/dni"),
        )
        assertEquals(
            OnboardingDestination.PROVIDER_SELFIE,
            OnboardingDeepLinkMapper.toInternalRoute("onboarding/identidad/selfie"),
        )
        assertEquals(
            OnboardingDestination.PROVIDER_CHECKLIST,
            OnboardingDeepLinkMapper.toInternalRoute("onboarding"),
        )
    }
}
