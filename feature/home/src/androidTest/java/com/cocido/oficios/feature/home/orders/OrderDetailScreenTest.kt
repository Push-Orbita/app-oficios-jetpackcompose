package com.cocido.oficios.feature.home.orders

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.BookingStatus
import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.model.ServiceRequestStatus
import com.cocido.oficios.domain.model.UserRole
import com.cocido.oficios.feature.home.TestHostActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrderDetailScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    private fun baseOrder(
        status: ServiceRequestStatus,
        bookingStatus: BookingStatus? = null,
        pendingCounterOffer: com.cocido.oficios.domain.model.CounterOffer? = null,
    ) = ServiceOrder(
        id = 1L,
        clientName = "Cliente Prueba",
        workerName = "Trabajador Prueba",
        workerId = 2L,
        rubroLabel = "Mascotas",
        subRubroLabel = "Peluquería canina",
        status = status,
        description = "",
        addressLabel = "San Martín, Av. Belgrano 1250",
        contactName = "Cliente Prueba",
        contactPhone = "3705123456",
        bookingStatus = bookingStatus,
        pendingCounterOffer = pendingCounterOffer,
    )

    // Regresión: antes esta combinación dejaba la pantalla en blanco después de la
    // tarjeta de info (sin acción visible ni explicación).
    @Test
    fun orderDetail_acceptedWithUnexpectedBookingStatus_showsFallbackInsteadOfBlankScreen() {
        composeTestRule.setContent {
            OficiosTheme {
                OrderDetailScreen(
                    uiState = OrderDetailUiState.Success(
                        order = baseOrder(
                            status = ServiceRequestStatus.ACCEPTED,
                            bookingStatus = BookingStatus.CANCELLED,
                        ),
                        viewerRole = UserRole.CLIENT,
                    ),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("No pudimos determinar el estado actual de este pedido.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    // Regresión: COUNTER_OFFER con pendingCounterOffer nulo (dato inconsistente) también
    // dejaba la pantalla en blanco.
    @Test
    fun orderDetail_counterOfferWithoutOfferData_showsFallbackInsteadOfBlankScreen() {
        composeTestRule.setContent {
            OficiosTheme {
                OrderDetailScreen(
                    uiState = OrderDetailUiState.Success(
                        order = baseOrder(
                            status = ServiceRequestStatus.COUNTER_OFFER,
                            pendingCounterOffer = null,
                        ),
                        viewerRole = UserRole.CLIENT,
                    ),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText("No pudimos cargar el detalle de la contrapropuesta. Vuelva a intentar más tarde.")
            .assertIsDisplayed()
    }

    @Test
    fun orderDetail_cancelDialog_usesOficiosDialogAndConfirms() {
        var cancelConfirmed = false
        composeTestRule.setContent {
            OficiosTheme {
                OrderDetailScreen(
                    uiState = OrderDetailUiState.Success(
                        order = baseOrder(status = ServiceRequestStatus.PENDING),
                        viewerRole = UserRole.CLIENT,
                    ),
                    onEvent = { event ->
                        if (event is OrderDetailEvent.CancelBeforeAcceptance) cancelConfirmed = true
                    },
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Cancelar solicitud").performClick()
        composeTestRule.onNodeWithText("¿Seguro que quiere cancelar este pedido?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sí, cancelar").performClick()

        assert(cancelConfirmed) { "El diálogo de cancelar no disparó CancelBeforeAcceptance" }
    }

    @Test
    fun orderDetail_rejectDialog_requiresReasonBeforeConfirming() {
        var rejectedReason: String? = null
        composeTestRule.setContent {
            OficiosTheme {
                OrderDetailScreen(
                    uiState = OrderDetailUiState.Success(
                        order = baseOrder(status = ServiceRequestStatus.PENDING),
                        viewerRole = UserRole.PROVIDER,
                    ),
                    onEvent = { event ->
                        if (event is OrderDetailEvent.Reject) rejectedReason = event.reason
                    },
                    onNavigateBack = {},
                )
            }
        }

        // La pantalla ya tiene un botón "Rechazar" (abre el diálogo) y el diálogo agrega
        // un segundo "Rechazar" (confirmar) — se usa el último nodo para cada uno.
        composeTestRule.onAllNodesWithText("Rechazar").onLast().performClick()
        composeTestRule.onNodeWithText("Rechazar solicitud").assertIsDisplayed()

        // Sin motivo, el botón de confirmar debe estar deshabilitado.
        composeTestRule.onAllNodesWithText("Rechazar").onLast().assertIsNotEnabled()

        composeTestRule.onNodeWithText("Motivo").performTextInput("No estoy disponible ese día")
        composeTestRule.onAllNodesWithText("Rechazar").onLast().performClick()

        assert(rejectedReason == "No estoy disponible ese día") {
            "Se esperaba que Reject se dispare con el motivo ingresado, pero fue: $rejectedReason"
        }
    }
}
