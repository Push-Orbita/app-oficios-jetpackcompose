package com.cocido.oficios.core.common.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PushDeepLinkMapperTest {

    @Test
    fun `maps oficios scheme order deep link`() {
        assertEquals("orders/42", PushDeepLinkMapper.toInternalRoute("oficios://orders/42"))
    }

    @Test
    fun `maps relative order deep link`() {
        assertEquals("orders/42", PushDeepLinkMapper.toInternalRoute("orders/42"))
    }

    @Test
    fun `maps kyc onboarding deep link`() {
        assertEquals(
            "onboarding/provider-checklist",
            PushDeepLinkMapper.toInternalRoute("oficios://onboarding/identidad"),
        )
    }

    @Test
    fun `maps notifications inbox`() {
        assertEquals("notifications", PushDeepLinkMapper.toInternalRoute("notifications"))
    }

    @Test
    fun `blank deep link returns null`() {
        assertNull(PushDeepLinkMapper.toInternalRoute("   "))
        assertNull(PushDeepLinkMapper.toInternalRoute(null))
    }
}
