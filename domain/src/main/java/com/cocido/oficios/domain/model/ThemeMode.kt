package com.cocido.oficios.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

fun ThemeMode.isDark(systemInDarkTheme: Boolean): Boolean = when (this) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> systemInDarkTheme
}
