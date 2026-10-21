package com.cocido.oficios.core.data.cache

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionCacheCoordinatorTest {

    @Test
    fun `clearAll empties every registered cache`() {
        val coordinator = SessionCacheCoordinator()
        val gallery = CachedValue<List<String>>(coordinator)
        val unread = CachedValue<Int>(coordinator)
        gallery.set(listOf("foto"))
        unread.set(4)

        coordinator.clearAll()

        assertNull(gallery.get())
        assertNull(unread.get())
        assertNull(gallery.state.value)
        assertEquals(null, unread.state.value)
    }
}
