package com.cocido.oficios.feature.home.agenda

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.domain.model.AgendaSlot
import com.cocido.oficios.domain.model.AgendaSlotStatus
import com.cocido.oficios.domain.model.ProviderSubRubroOption
import com.cocido.oficios.feature.home.TestHostActivity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AgendaListScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHostActivity>()

    private val selectedDate = LocalDate.of(2030, 3, 12)
    private val option = ProviderSubRubroOption(
        id = 4L,
        subRubroLabel = "Peluquería canina",
        rubroLabel = "Mascotas",
        requiresTurns = true,
    )
    private val slot = AgendaSlot(
        id = 20L,
        subRubroId = option.id,
        subRubroLabel = option.subRubroLabel,
        start = LocalDateTime.of(2030, 3, 12, 10, 0),
        end = LocalDateTime.of(2030, 3, 12, 11, 0),
        status = AgendaSlotStatus.DISPONIBLE,
        autoAcceptance = false,
    )

    private fun successState(
        slots: List<AgendaSlot> = listOf(slot),
        showSlotForm: Boolean = false,
        slotStart: LocalDateTime? = null,
        slotEnd: LocalDateTime? = null,
        pendingDeleteId: Long? = null,
    ) = AgendaListUiState.Success(
        subRubroOptions = listOf(option),
        selectedSubRubroId = option.id,
        slots = slots,
        visibleMonth = YearMonth.of(2030, 3),
        selectedDate = selectedDate,
        showSlotForm = showSlotForm,
        slotStart = slotStart,
        slotEnd = slotEnd,
        slotPendingDeleteId = pendingDeleteId,
    )

    @Test
    fun agenda_month_showsSelectedDayAndItsAvailability() {
        composeTestRule.setContent {
            OficiosTheme {
                AgendaListScreen(successState(), onEvent = {}, onNavigateBack = {})
            }
        }

        composeTestRule.onNodeWithText("Marzo 2030").assertIsDisplayed()
        composeTestRule.onNodeWithText("Martes 12 de marzo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agenda_list").performScrollToIndex(4)
        composeTestRule.onNodeWithText("10:00 – 11:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Peluquería canina").assertIsDisplayed()
    }

    @Test
    fun agenda_overlapExplainsTheProblemAndDisablesSave() {
        composeTestRule.setContent {
            OficiosTheme {
                AgendaListScreen(
                    uiState = successState(
                        showSlotForm = true,
                        slotStart = LocalDateTime.of(2030, 3, 12, 10, 30),
                        slotEnd = LocalDateTime.of(2030, 3, 12, 11, 30),
                    ),
                    onEvent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Este horario se superpone con otro turno de tu agenda.")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Guardar disponibilidad")
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun agenda_reservedSlotCannotBeEditedOrDeleted() {
        val reserved = slot.copy(status = AgendaSlotStatus.RESERVADO, bookingId = 99L)
        composeTestRule.setContent {
            OficiosTheme {
                AgendaListScreen(successState(slots = listOf(reserved)), onEvent = {}, onNavigateBack = {})
            }
        }

        composeTestRule.onNodeWithTag("agenda_list").performScrollToIndex(4)
        composeTestRule.onNodeWithText("Reservado").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reserva #99").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Editar disponibilidad").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Eliminar disponibilidad").assertDoesNotExist()
    }

    @Test
    fun agenda_deleteDialogRequiresExplicitConfirmation() {
        var confirmed = false
        composeTestRule.setContent {
            OficiosTheme {
                AgendaListScreen(
                    uiState = successState(pendingDeleteId = slot.id),
                    onEvent = { if (it == AgendaListEvent.ConfirmDeleteSlot) confirmed = true },
                    onNavigateBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Eliminar disponibilidad").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eliminar").performClick()
        assertTrue(confirmed)
    }
}
