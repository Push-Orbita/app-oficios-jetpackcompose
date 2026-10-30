package com.cocido.oficios.feature.home.orders

import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceRequestStatus
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

    @Test
    fun qualityStarsUsesOneToFiveScaleAndToleratesLegacyTen() {
        assertEquals(5, qualityStars(5))
        assertEquals(4, qualityStars(4))
        assertEquals(1, qualityStars(1))
        assertEquals(5, qualityStars(10))
        assertEquals(5, qualityStars(9))
        assertEquals(4, qualityStars(8))
        assertNull(qualityStars(0))
        assertNull(qualityStars(null))
    }

    @Test
    fun finishedBookingShowsRateActionAndDetailTitle() {
        assertEquals(
            ClientPrimaryAction.RateWorker,
            clientPrimaryAction(ServiceRequestStatus.ACCEPTED, BookingStatus.FINISHED, hasContact = true, alreadyRated = false),
        )
        assertEquals(
            ClientPrimaryAction.ShowRating,
            clientPrimaryAction(ServiceRequestStatus.ACCEPTED, BookingStatus.FINISHED, hasContact = true, alreadyRated = true),
        )
        assertEquals(R.string.orders_title_detail, orderScreenTitleRes(ServiceRequestStatus.ACCEPTED, BookingStatus.FINISHED))
    }
}
