package com.cocido.oficios

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import com.cocido.oficios.navigation.OficiosApp
import com.cocido.oficios.ui.MainViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OficiosRoot(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val pendingPushRoute by viewModel.pendingPushRoute.collectAsStateWithLifecycle()

    // El diseño de Oficios Formosa tiene un único modo (navy oscuro, "Mode 1" del Figma).
    // No hay assets ni tokens verificados para modo claro, así que se fuerza siempre
    // oscuro sin importar el tema del sistema — evita pantallas rotas (logo blanco
    // invisible, contraste incorrecto) en dispositivos con el sistema en claro.
    OficiosTheme(darkTheme = true) {
        OficiosApp(
            pendingPushRoute = pendingPushRoute,
            onPushRouteConsumed = viewModel::consumePushRoute,
        )
    }
}
