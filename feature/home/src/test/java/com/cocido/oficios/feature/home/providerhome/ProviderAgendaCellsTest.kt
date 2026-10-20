package com.cocido.oficios.feature.home.providerhome

import com.cocido.oficios.feature.home.providerhome.components.agendaMonthCells
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El mini-calendario del Home de trabajador se dibuja como grilla no-lazy que se adapta
 * al font scale. Estos tests fijan el contrato de [agendaMonthCells]: la grilla siempre
 * cubre todos los dias del mes (incluida la 5a/6a semana, p. ej. 26-31) sin recortar.
 */
class ProviderAgendaCellsTest {

    @Test
    fun `la grilla cubre todos los dias del mes sin recortes`() {
        // Marzo 2030 empieza en viernes y tiene 31 dias: fuerza una 6a fila con dias 26-31.
        val month = YearMonth.of(2030, 3)
        val cells = agendaMonthCells(month)

        val days = cells.filterNotNull()
        assertEquals(month.lengthOfMonth(), days.size)
        // Todos los dias del 1 al ultimo estan presentes.
        (1..month.lengthOfMonth()).forEach { day ->
            assertTrue("Falta el dia $day", month.atDay(day) in days)
        }
        // La ultima semana (26-31) no se pierde.
        (26..31).forEach { day ->
            assertTrue("Falta el dia $day de la ultima semana", LocalDate.of(2030, 3, day) in days)
        }
    }

    @Test
    fun `la grilla es siempre multiplo de 7 y arranca en lunes`() {
        // Varios meses con distinto dia de inicio y largo.
        listOf(
            YearMonth.of(2026, 2), // febrero (28 dias) que arranca domingo
            YearMonth.of(2026, 3),
            YearMonth.of(2024, 2), // bisiesto, 29 dias
            YearMonth.of(2030, 11),
        ).forEach { month ->
            val cells = agendaMonthCells(month)
            assertEquals("Debe ser multiplo de 7 en $month", 0, cells.size % 7)

            val leadingBlanks = month.atDay(1).dayOfWeek.value - 1
            // El primer dia del mes cae exactamente en la columna de su dia de semana.
            assertEquals("Hueco inicial incorrecto en $month", month.atDay(1), cells[leadingBlanks])
            repeat(leadingBlanks) { index ->
                assertEquals("La celda $index deberia ser hueco en $month", null, cells[index])
            }
        }
    }
}
