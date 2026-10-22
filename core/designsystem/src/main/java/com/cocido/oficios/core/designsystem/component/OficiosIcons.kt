package com.cocido.oficios.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.cocido.oficios.core.designsystem.R

/**
 * Iconos de Oficios — Lucide icons como VectorDrawables
 *
 * Estos iconos reemplazan Material Icons en el diseño de Figma.
 * Fuente: Lucide (https://lucide.dev/), licencia ISC.
 *
 * Uso:
 * ```
 * Icon(
 *     imageVector = ImageVector.vectorResource(OficiosIcons.Star),
 *     contentDescription = "Calificación"
 * )
 * ```
 *
 * Para iconos temáticos (tintColor automático), usar directamente el @DrawableRes:
 * ```
 * Icon(
 *     painter = painterResource(OficiosIcons.Star),
 *     contentDescription = "Calificación",
 *     tint = MaterialTheme.colorScheme.primary
 * )
 * ```
 */
object OficiosIcons {
    // Navegación y acciones básicas
    @DrawableRes val ChevronLeft = R.drawable.ic_lucide_chevron_left
    @DrawableRes val ChevronRight = R.drawable.ic_lucide_chevron_right
    @DrawableRes val ChevronDown = R.drawable.ic_lucide_chevron_down
    @DrawableRes val Check = R.drawable.ic_lucide_check
    @DrawableRes val Menu = R.drawable.ic_lucide_menu
    @DrawableRes val X = R.drawable.ic_lucide_x
    @DrawableRes val Search = R.drawable.ic_lucide_search
    @DrawableRes val Mic = R.drawable.ic_lucide_mic
    @DrawableRes val SlidersHorizontal = R.drawable.ic_lucide_sliders_horizontal
    
    // Usuarios y perfiles
    @DrawableRes val User = R.drawable.ic_lucide_user
    @DrawableRes val Users = R.drawable.ic_lucide_users
    @DrawableRes val BadgeCheck = R.drawable.ic_lucide_badge_check
    @DrawableRes val ShieldCheck = R.drawable.ic_lucide_shield_check
    @DrawableRes val Camera = R.drawable.ic_lucide_camera
    @DrawableRes val BookUser = R.drawable.ic_lucide_book_user
    
    // Ubicación y navegación
    @DrawableRes val House = R.drawable.ic_lucide_house
    @DrawableRes val MapPinHouse = R.drawable.ic_lucide_map_pin_house
    
    // Notificaciones y alertas
    @DrawableRes val BellDot = R.drawable.ic_lucide_bell_dot
    @DrawableRes val CircleAlert = R.drawable.ic_lucide_circle_alert
    @DrawableRes val CircleCheck = R.drawable.ic_lucide_circle_check
    @DrawableRes val CircleDashed = R.drawable.ic_lucide_circle_dashed
    @DrawableRes val CirclePlus = R.drawable.ic_lucide_circle_plus
    @DrawableRes val CircleQuestionMark = R.drawable.ic_lucide_circle_question_mark
    @DrawableRes val Circle = R.drawable.ic_lucide_circle
    @DrawableRes val CircleDot = R.drawable.ic_lucide_circle_dot
    
    // Calificaciones y favoritos
    @DrawableRes val Star = R.drawable.ic_lucide_star
    @DrawableRes val StarHalf = R.drawable.ic_lucide_star_half
    @DrawableRes val Heart = R.drawable.ic_lucide_heart
    
    // Tiempo y calendario
    @DrawableRes val Clock = R.drawable.ic_lucide_clock
    @DrawableRes val ClockCheck = R.drawable.ic_lucide_clock_check
    
    // Documentos y pedidos
    @DrawableRes val ReceiptText = R.drawable.ic_lucide_receipt_text
    @DrawableRes val ScrollText = R.drawable.ic_lucide_scroll_text
    
    // Dinero y pagos
    @DrawableRes val DollarSign = R.drawable.ic_lucide_dollar_sign
    @DrawableRes val Banknote = R.drawable.ic_lucide_banknote
    
    // Edición y acciones
    @DrawableRes val Pencil = R.drawable.ic_lucide_pencil
    @DrawableRes val Redo = R.drawable.ic_lucide_redo
    @DrawableRes val RotateCw = R.drawable.ic_lucide_rotate_cw
    @DrawableRes val RefreshCw = R.drawable.ic_lucide_refresh_cw
    @DrawableRes val Eye = R.drawable.ic_lucide_eye
    @DrawableRes val EyeClosed = R.drawable.ic_lucide_eye_closed
    
    // Oficios y herramientas
    @DrawableRes val Hammer = R.drawable.ic_lucide_hammer
    
    // Varios
    @DrawableRes val Asterisk = R.drawable.ic_lucide_asterisk
    @DrawableRes val Key = R.drawable.ic_lucide_key
    @DrawableRes val BatteryLow = R.drawable.ic_lucide_battery_low
    @DrawableRes val Signal = R.drawable.ic_lucide_signal
    @DrawableRes val Wifi = R.drawable.ic_lucide_wifi
    
    // Iconos legacy de Oficios (mantenidos para compatibilidad)
    @DrawableRes val Casco = R.drawable.ic_casco
    @DrawableRes val Lupa = R.drawable.ic_lupa
    @DrawableRes val LogotipoTitle = R.drawable.ic_oficios_logotipo_title
    @DrawableRes val LogotipoTitleOutline = R.drawable.ic_oficios_logotipo_title_outline
    @DrawableRes val Logotipo = R.drawable.ic_oficios_logotipo
    @DrawableRes val LogotipoOutline = R.drawable.ic_oficios_logotipo_outline
}

/**
 * Helper composable para cargar iconos Lucide como ImageVector
 */
@Composable
fun rememberOficiosIcon(@DrawableRes id: Int): ImageVector {
    return ImageVector.vectorResource(id)
}
