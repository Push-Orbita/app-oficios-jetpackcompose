package com.cocido.oficios.domain.usecase.profile

import com.cocido.oficios.domain.model.ClientContacto
import com.cocido.oficios.domain.model.ClientContactoRequest
import com.cocido.oficios.domain.model.ClientDomicilio

/** Reglas puras para no duplicar contacto/domicilio cuando el cliente reintenta el onboarding. */
object ClientProfileReuse {

    /**
     * 1. Un contacto con el mismo WhatsApp (verificado o no): es el mismo número, se actualiza.
     * 2. Si no, el favorito (o el único) todavía no verificado: el usuario está corrigiendo el número.
     * Un contacto ya verificado con otro número no se pisa: se crea uno nuevo.
     */
    fun contactoToReuse(existing: List<ClientContacto>, request: ClientContactoRequest): ClientContacto? {
        if (existing.isEmpty()) return null
        val requestedPhone = phoneKey(request.telefonoWhatsapp)
        if (requestedPhone.isNotEmpty()) {
            existing.firstOrNull { phoneKey(it.telefonoWhatsapp) == requestedPhone }?.let { return it }
        }
        val primary = existing.firstOrNull { it.favorito } ?: existing.singleOrNull()
        return primary?.takeIf { !it.telefonoVerificado }
    }

    fun contactoNeedsUpdate(existing: ClientContacto, request: ClientContactoRequest): Boolean =
        existing.nombre.trim() != request.nombre.trim() ||
            phoneKey(existing.telefonoWhatsapp) != phoneKey(request.telefonoWhatsapp) ||
            existing.email.orEmpty().trim() != request.email.orEmpty().trim() ||
            existing.favorito != request.favorito

    fun domicilioToReuse(existing: List<ClientDomicilio>): ClientDomicilio? =
        existing.firstOrNull { it.favorito } ?: existing.singleOrNull()

    /**
     * El backend guarda "+543705219109"; la app manda "93705219109" o "3705219109". Se comparan los
     * últimos 10 dígitos (característica + número local en Argentina).
     */
    internal fun phoneKey(raw: String): String = raw.filter(Char::isDigit).takeLast(10)
}
