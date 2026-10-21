package com.cocido.oficios.core.designsystem.component.badges

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import com.cocido.oficios.core.designsystem.theme.OficiosColorSuccess
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
import com.cocido.oficios.core.designsystem.theme.OficiosTheme

/**
 * Badge de usuario verificado — [OficiosColorSuccess] (#4CAF7D) sobre navy.
 * Cliente: badge-check (Figma 25:95). Trabajador: shield-check (Figma 259:254).
 */
@Composable
fun OficiosVerifiedBadge(
    modifier: Modifier = Modifier,
    contentDescription: String? = stringResource(R.string.ds_verified),
    @DrawableRes iconRes: Int = OficiosIcons.BadgeCheck,
) {
    Icon(
        imageVector = ImageVector.vectorResource(iconRes),
        contentDescription = contentDescription,
        tint = OficiosColorSuccess,
        modifier = modifier.size(OficiosComponentSize.iconMd),
    )
}

@Preview(name = "Verified badge", showBackground = true, backgroundColor = 0xFF00358B)
@Composable
private fun OficiosVerifiedBadgePreview() {
    OficiosTheme {
        OficiosVerifiedBadge()
    }
}
