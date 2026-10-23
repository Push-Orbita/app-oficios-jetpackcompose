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

fun parseThemeMode(raw: String?): ThemeMode = when (raw) {
    ThemeMode.LIGHT.name -> ThemeMode.LIGHT
    ThemeMode.DARK.name -> ThemeMode.DARK
    ThemeMode.SYSTEM.name -> ThemeMode.SYSTEM
    else -> ThemeMode.LIGHT
}
