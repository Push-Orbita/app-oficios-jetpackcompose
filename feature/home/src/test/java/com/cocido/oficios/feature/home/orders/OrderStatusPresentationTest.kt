package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.feature.home.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrderStatusPresentationTest {

    @Test
    fun cancelledByAdminShowsSupportCopy() {
        assertEquals(
            R.string.orders_cancelled_by_support,
            cancelledByLabelRes("ADMIN"),
        )
        assertEquals(
            R.string.orders_cancelled_by_client,
            cancelledByLabelRes("cliente"),
        )
        assertNull(cancelledByLabelRes(null))
    }
}
