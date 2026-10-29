package com.cocido.oficios.feature.settings.navigation

object SettingsDestination {
    const val route = "settings"
    const val personalData = "settings/personal-data"
    const val clientData = "settings/client-data"
    const val professionalData = "settings/professional-data"
    const val services = "settings/services"
    const val addService = "settings/services/add"
    const val editService = "settings/services/edit/{serviceId}"
    fun editService(serviceId: Long) = "settings/services/edit/$serviceId"
    const val FOCUS_GALLERY = "gallery"
    const val FOCUS_CERTIFICATE = "certificate"
    const val portfolio = "settings/portfolio?focus={focus}"
    fun portfolio(focus: String = FOCUS_GALLERY) = "settings/portfolio?focus=$focus"
    const val commercialLocal = "settings/commercial-local"
    const val changePassword = "settings/change-password"
    const val deleteAccount = "settings/delete-account"
    const val configuration = "settings/configuration"
    const val verifyEmail = "settings/verify-email"
}
