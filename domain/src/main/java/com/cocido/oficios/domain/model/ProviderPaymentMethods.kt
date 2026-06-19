package com.cocido.oficios.domain.model

object ProviderPaymentMethods {
    const val EFECTIVO = "EFECTIVO"
    const val TRANSFERENCIA = "TRANSFERENCIA"
    const val MERCADOPAGO = "MERCADOPAGO"

    val all = listOf(EFECTIVO, TRANSFERENCIA, MERCADOPAGO)
}
