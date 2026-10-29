package com.cocido.oficios.core.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PublicEndpointsTest {

    @Test
    fun `email verification endpoints require bearer`() {
        assertFalse(PublicEndpoints.isPublic("auth/verify-email"))
        assertFalse(PublicEndpoints.isPublic("/auth/verify-email"))
        assertFalse(PublicEndpoints.isPublic("auth/send-verification-email"))
        assertFalse(PublicEndpoints.isPublic("/auth/send-verification-email"))
    }

    @Test
    fun `login signup and password reset request stay public`() {
        assertTrue(PublicEndpoints.isPublic("auth/login"))
        assertTrue(PublicEndpoints.isPublic("/auth/login"))
        assertTrue(PublicEndpoints.isPublic("auth/signup"))
        assertTrue(PublicEndpoints.isPublic("auth/refresh"))
        assertTrue(PublicEndpoints.isPublic("auth/password-reset/request-code"))
        assertTrue(PublicEndpoints.isPublic("auth/password-reset/verify-code"))
    }

    @Test
    fun `contains matching cannot treat verify-email as login`() {
        assertFalse(PublicEndpoints.isPublic("auth/verify-email/confirm"))
        assertFalse(PublicEndpoints.isPublic("oauth/login"))
    }
}
