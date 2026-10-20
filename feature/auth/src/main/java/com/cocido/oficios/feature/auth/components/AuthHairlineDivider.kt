package com.cocido.oficios.feature.auth.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosColorAuthDivider

@Composable
fun AuthHairlineDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = OficiosColorAuthDivider,
    )
}
