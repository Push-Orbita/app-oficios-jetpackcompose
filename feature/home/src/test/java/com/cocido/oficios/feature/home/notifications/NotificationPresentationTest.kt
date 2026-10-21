package com.cocido.oficios.feature.home.notifications

import com.cocido.oficios.core.designsystem.component.OficiosIcons
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationPresentationTest {

    @Test
    fun `maps catalog types to existing lucide icons`() {
        assertEquals(OficiosIcons.CirclePlus, notificationIconRes("NUEVA_SOLICITUD"))
        assertEquals(OficiosIcons.CircleCheck, notificationIconRes("SOLICITUD_ACEPTADA"))
        assertEquals(OficiosIcons.CircleAlert, notificationIconRes("SOLICITUD_RECHAZADA"))
        assertEquals(OficiosIcons.CircleAlert, notificationIconRes("SOLICITUD_CANCELADA"))
        assertEquals(OficiosIcons.ScrollText, notificationIconRes("CONTRAPROPUESTA_RECIBIDA"))
        assertEquals(OficiosIcons.Star, notificationIconRes("CALIFICACION_PENDIENTE"))
        assertEquals(OficiosIcons.ShieldCheck, notificationIconRes("KYC_REVISION_DOCUMENTACION"))
        assertEquals(OficiosIcons.BellDot, notificationIconRes("OTRO"))
    }

    @Test
    fun `cancel in type uses alert icon`() {
        assertEquals(OficiosIcons.CircleAlert, notificationIconRes("pedido_cancelado"))
    }
}
