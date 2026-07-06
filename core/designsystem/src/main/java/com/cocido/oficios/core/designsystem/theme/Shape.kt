package com.cocido.oficios.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

// ---------------------------------------------------------------------------
// Shapes — Oficios Formosa
//
// Uso recomendado:
//   extraSmall (4dp)  → badges, indicadores pequeños
//   small (8dp)       → botones, inputs, chips, avatares
//   medium (12dp)     → diálogos pequeños
//   large (16dp)      → cards principales, bottom sheets
//   extraLarge (24dp) → modales grandes, paneles
// ---------------------------------------------------------------------------

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(OficiosRadius.xs),
    small = RoundedCornerShape(OficiosRadius.sm),
    medium = RoundedCornerShape(OficiosRadius.md),
    large = RoundedCornerShape(OficiosRadius.lg),
    extraLarge = RoundedCornerShape(OficiosRadius.xl),
)
