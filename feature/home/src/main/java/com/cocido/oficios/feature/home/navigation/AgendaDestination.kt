package com.cocido.oficios.feature.home.navigation

object AgendaDestination {
    const val list = "agenda"
    const val slots = "agenda/slots"
    const val config = "agenda/config"
    const val availability = "agenda/availability"
    const val weekly = "agenda/availability/{subRubroId}"
    fun weekly(subRubroId: Long) = "agenda/availability/$subRubroId"
    const val blocks = "agenda/availability/{subRubroId}/bloqueos"
    fun blocks(subRubroId: Long) = "agenda/availability/$subRubroId/bloqueos"
}
