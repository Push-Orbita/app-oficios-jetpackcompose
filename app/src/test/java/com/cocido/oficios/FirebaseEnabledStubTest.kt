package com.cocido.oficios

import org.junit.Assert.assertFalse
import org.junit.Test

class FirebaseEnabledStubTest {

    @Test
    fun stubGoogleServicesKeepsFirebaseDisabled() {
        assertFalse(BuildConfig.FIREBASE_ENABLED)
    }
}
