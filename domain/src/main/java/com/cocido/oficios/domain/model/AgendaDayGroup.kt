package com.cocido.oficios.domain.model

import com.cocido.oficios.core.common.time.DisplayDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

data class AgendaDayGroup(
    val date: LocalDate,
    val headerLabel: String,
    val isToday: Boolean,
    val items: List<ServiceOrderListItem>,
)

fun ServiceOrderListItem.scheduledDate(): LocalDate? {
    val raw = scheduledSortKey?.trim().orEmpty()
    if (raw.isEmpty()) return null
    return DisplayDateTime.toZonedDateTime(raw)?.toLocalDate()
        ?: runCatching { LocalDate.parse(raw.take(10)) }.getOrNull()
}

fun ServiceOrderListItem.agendaTimeLabel(): String {
    DisplayDateTime.toZonedDateTime(scheduledSortKey)?.toLocalTime()?.let { time ->
        return time.toAgendaClock()
    }
    val isoTime = scheduledSortKey?.substringAfter('T', "").orEmpty().take(5)
    if (isoTime.length == 5 && isoTime[2] == ':') return "${isoTime}hs"
    val fromLabel = Regex("""(\d{1,2}:\d{2})""").find(scheduledLabel.orEmpty())?.groupValues?.get(1)
    if (fromLabel != null) {
        val padded = if (fromLabel.length == 4) "0$fromLabel" else fromLabel
        return if (padded.endsWith("hs")) padded else "${padded}hs"
    }
    return scheduledLabel.orEmpty()
}

fun ServiceOrderListItem.agendaRubroLabel(): String =
    rubroLabel.ifBlank { subRubroLabel }

fun groupAgendaAppointments(
    orders: List<ServiceOrderListItem>,
    today: LocalDate,
    locale: Locale = Locale.forLanguageTag("es-AR"),
    newestFirst: Boolean = false,
): List<AgendaDayGroup> {
    val grouped = linkedMapOf<LocalDate, MutableList<ServiceOrderListItem>>()
    orders.forEach { item ->
        val date = item.scheduledDate() ?: return@forEach
        grouped.getOrPut(date) { mutableListOf() }.add(item)
    }
    val dates = if (newestFirst) {
        grouped.entries.sortedByDescending { it.key }
    } else {
        grouped.entries.sortedBy { it.key }
    }
    return dates.map { (date, items) ->
        val month = date.month.getDisplayName(TextStyle.FULL, locale).lowercase(locale)
        val sortedItems = if (newestFirst) {
            items.sortedWith(
                compareByDescending<ServiceOrderListItem> { it.scheduledSortKey.orEmpty() }
                    .thenByDescending { it.id },
            )
        } else {
            items.sortedWith(
                compareBy<ServiceOrderListItem> { it.scheduledSortKey.orEmpty() }
                    .thenBy { it.id },
            )
        }
        AgendaDayGroup(
            date = date,
            headerLabel = "${date.dayOfMonth} de $month",
            isToday = date == today,
            items = sortedItems,
        )
    }
}

private fun LocalTime.toAgendaClock(): String =
    "%02d:%02dhs".format(hour, minute)
