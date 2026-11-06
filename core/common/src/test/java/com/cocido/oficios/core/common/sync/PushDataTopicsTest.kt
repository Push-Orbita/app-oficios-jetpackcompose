package com.cocido.oficios.core.common.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class PushDataTopicsTest {

    @Test
    fun `kyc review refreshes profile status and notifications`() {
        assertEquals(
            setOf(DataTopic.NOTIFICATIONS, DataTopic.PROFILE_STATUS),
            PushDataTopics.forPush("KYC_REVISION_DOCUMENTACION"),
        )
    }

    @Test
    fun `order events refresh orders`() {
        listOf(
            "NUEVA_SOLICITUD",
            "SOLICITUD_ACEPTADA",
            "SOLICITUD_RECHAZADA",
            "CONTRAPROPUESTA_RECIBIDA",
            "CONTRAPROPUESTA_ACEPTADA",
            "TRABAJADOR_EN_CAMINO",
            "TRABAJADOR_LLEGO",
            "CALIFICACION_PENDIENTE",
        ).forEach { type ->
            assertEquals(type, setOf(DataTopic.NOTIFICATIONS, DataTopic.ORDERS), PushDataTopics.forPush(type))
        }
    }

    @Test
    fun `booking events refresh orders and agenda`() {
        listOf("BOOKING_CONFIRMADO", "SERVICIO_FINALIZADO").forEach { type ->
            assertEquals(
                type,
                setOf(DataTopic.NOTIFICATIONS, DataTopic.ORDERS, DataTopic.AGENDA),
                PushDataTopics.forPush(type),
            )
        }
    }

    @Test
    fun `unknown type still refreshes notifications and entity type is honored`() {
        assertEquals(setOf(DataTopic.NOTIFICATIONS), PushDataTopics.forPush("GENERIC"))
        assertEquals(setOf(DataTopic.NOTIFICATIONS), PushDataTopics.forPush(null))
        assertEquals(
            setOf(DataTopic.NOTIFICATIONS, DataTopic.ORDERS),
            PushDataTopics.forPush("OTRO", entityType = "solicitud"),
        )
    }
}
