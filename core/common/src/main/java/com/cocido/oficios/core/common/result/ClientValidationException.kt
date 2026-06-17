package com.cocido.oficios.core.common.result

/**
 * Error de validación local antes de llamar al backend.
 * El mensaje se muestra tal cual al usuario.
 */
class ClientValidationException(
    override val message: String,
) : Exception(message)
