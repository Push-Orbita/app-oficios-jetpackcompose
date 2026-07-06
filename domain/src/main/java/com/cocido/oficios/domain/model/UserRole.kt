package com.cocido.oficios.domain.model

enum class UserRole {
    CLIENT,
    PROVIDER,
    UNKNOWN,
    ;

    companion object {
        fun fromRoleName(name: String?): UserRole = when {
            name == null -> UNKNOWN
            name.contains("cliente", ignoreCase = true) -> CLIENT
            name.contains("trabajador", ignoreCase = true) -> PROVIDER
            else -> UNKNOWN
        }
    }
}
