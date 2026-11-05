package com.cocido.oficios.feature.auth.navigation

import com.cocido.oficios.domain.model.RegistrationRole

object AuthDestination {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER_ROLE = "register/role-selection"
    const val REGISTER_CLIENT = "register/client"
    const val REGISTER_TRADE = "register/trade"
    const val REGISTER_PROVIDER = "register/provider"
    const val FORGOT_PASSWORD = "forgot-password"
    const val RESET_PASSWORD = "reset-password"

    const val WIZARD_ROLE_ARG = "role"
    const val WIZARD_GRAPH = "register/wizard/{$WIZARD_ROLE_ARG}"
    const val WIZARD_PERSONAL = "wizard_personal"
    const val WIZARD_PROFILE = "wizard_profile"
    const val WIZARD_OFICIO = "wizard_oficio"
    const val WIZARD_ZONA = "wizard_zona"
    const val WIZARD_AGENDA = "wizard_agenda"
    const val WIZARD_COBRO = "wizard_cobro"
    const val WIZARD_IDENTIDAD = "wizard_identidad"
    const val WIZARD_REVIEW = "wizard_review"

    fun wizardGraph(role: RegistrationRole): String = "register/wizard/${role.name}"
}
