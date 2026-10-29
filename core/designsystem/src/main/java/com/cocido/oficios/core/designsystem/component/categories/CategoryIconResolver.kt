package com.cocido.oficios.core.designsystem.component.categories

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Carpenter
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ElectricalServices
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MiscellaneousServices
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PestControl
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Plumbing
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Roofing
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Window
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material.icons.outlined.Yard
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.cocido.oficios.core.designsystem.R
import com.cocido.oficios.core.designsystem.component.OficiosIcons
import java.text.Normalizer

/**
 * Iconos de categoría / rubro / subrubro.
 *
 * Prioridad: URL remota (admin) → ilustración de oficio tintada → mapeo por
 * nombre/id/slug → set genérico hasheado (nunca el martillo universal).
 */
data class CategoryIconSpec(
    val remoteUrl: String? = null,
    @DrawableRes val illustrationRes: Int? = null,
    val vector: CategoryVectorIcon,
)

sealed interface CategoryVectorIcon {
    data class Lucide(@DrawableRes val resId: Int) : CategoryVectorIcon
    data class Material(val imageVector: ImageVector) : CategoryVectorIcon
}

object CategoryIconResolver {

    @Composable
    fun iconFor(categoryId: Long, categoryName: String): ImageVector {
        return when (val vector = resolve(categoryId, categoryName).vector) {
            is CategoryVectorIcon.Lucide -> ImageVector.vectorResource(vector.resId)
            is CategoryVectorIcon.Material -> vector.imageVector
        }
    }

    fun resolve(
        categoryId: Long,
        categoryName: String,
        iconUrl: String? = null,
    ): CategoryIconSpec {
        return CategoryIconSpec(
            remoteUrl = sanitizeUrl(iconUrl),
            illustrationRes = illustrationRes(categoryName),
            vector = vectorFallback(categoryId, categoryName),
        )
    }

    @DrawableRes
    fun illustrationRes(name: String): Int? {
        val key = normalize(name)
        if (key.isEmpty()) return null
        return when {
            key.contains("alba") -> R.drawable.ill_albanil
            key.contains("plom") -> R.drawable.ill_plomero
            key.contains("instal") && !key.contains("electron") -> R.drawable.ill_instalador
            key.contains("pint") -> R.drawable.ill_pintor
            key.contains("carpin") -> R.drawable.ill_carpintero
            isGasTrade(key) -> R.drawable.ill_gasista
            key.contains("electr") && !key.contains("electron") -> R.drawable.ill_electricista
            key.contains("ingen") -> R.drawable.ill_ingeniero
            key.contains("manten") || key.contains("refacc") -> R.drawable.ill_mantenimiento
            key.contains("bienestar") -> R.drawable.ill_bienestar
            // Hogar / Construcción / Seguridad / Personales: ilustraciones blancas
            // de Figma no distinguen el rubro (o son ilegibles en cards claras).
            else -> null
        }
    }

    /**
     * Drawable Lucide cuando el fallback es Lucide; si el mapeo es Material
     * no hay res id — los callers nuevos deben usar [resolve].
     */
    @DrawableRes
    fun vectorRes(categoryId: Long, categoryName: String): Int {
        return when (val vector = vectorFallback(categoryId, categoryName)) {
            is CategoryVectorIcon.Lucide -> vector.resId
            is CategoryVectorIcon.Material -> OficiosIcons.House
        }
    }

    fun isIllustration(name: String): Boolean = illustrationRes(name) != null

    internal fun normalize(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        val decomposed = Normalizer.normalize(trimmed.lowercase(), Normalizer.Form.NFD)
        return ACCENT_MARKS.replace(decomposed, "")
            .replace('_', ' ')
            .replace('-', ' ')
            .replace(WHITESPACE, " ")
            .trim()
    }

    private fun vectorFallback(categoryId: Long, categoryName: String): CategoryVectorIcon {
        val key = normalize(categoryName)
        if (key.isEmpty()) return hashPick(categoryId, categoryName)
        mappedVector(key)?.let { return it }
        return hashPick(categoryId, key)
    }

    private fun mappedVector(key: String): CategoryVectorIcon? = when {
        matches(key, "todo") || key.contains("ver toda") || key.contains("see all") ->
            lucide(OficiosIcons.BookUser)

        matches(key, "segur", "vigil") -> lucide(OficiosIcons.ShieldCheck)
        matches(key, "personal") || isShortToken(key, "user") -> lucide(OficiosIcons.Users)
        matches(key, "cerr") -> lucide(OficiosIcons.Key)

        matches(key, "veterin", "canin", "felin", "mascota") ->
            material(Icons.Outlined.Pets)
        matches(key, "tecnolog", "informat", "comput", "digital", "software", "program") ||
            key.contains("electronica") ->
            material(Icons.Outlined.Devices)
        matches(key, "salud", "medic", "clinic", "enfermer", "odont", "kinesi") ->
            material(Icons.Outlined.HealthAndSafety)
        matches(key, "belleza", "estetic", "maquill") || isShortToken(key, "spa") ->
            material(Icons.Outlined.Spa)
        matches(key, "peluqu", "barber") ->
            material(Icons.Outlined.ContentCut)
        matches(key, "evento", "fiesta", "celebr", "catering") ->
            material(Icons.Outlined.Event)
        matches(key, "metal", "herrer", "soldad", "fundic", "acero", "hierro") ->
            material(Icons.Outlined.Build)
        isShortToken(key, "test") || matches(key, "prueba") || isShortToken(key, "demo") ->
            material(Icons.Outlined.Science)
        matches(key, "hogar", "domest") ->
            lucide(OficiosIcons.House)
        matches(key, "construc") || isShortToken(key, "obra") ->
            material(Icons.Outlined.Construction)

        matches(key, "plom", "sanitar") -> material(Icons.Outlined.Plumbing)
        matches(key, "electr") && !key.contains("electron") ->
            material(Icons.Outlined.ElectricalServices)
        matches(key, "pint") -> material(Icons.Outlined.FormatPaint)
        matches(key, "carpin") -> material(Icons.Outlined.Carpenter)
        isGasTrade(key) -> material(Icons.Outlined.LocalFireDepartment)
        matches(key, "limpieza", "aseo") ->
            material(Icons.Outlined.CleaningServices)
        matches(key, "auto", "mecan", "taller", "vehic") ->
            material(Icons.Outlined.DirectionsCar)
        matches(key, "gastro", "cocin", "chef", "restau", "comida") ->
            material(Icons.Outlined.Restaurant)
        matches(key, "educa", "clase", "profesor", "tutoria") ->
            material(Icons.Outlined.School)
        matches(key, "legal", "abog", "escriban", "notaria") ->
            material(Icons.Outlined.Balance)
        matches(key, "transporte", "flete", "mudanza", "logist") ->
            material(Icons.Outlined.LocalShipping)
        matches(key, "niner", "ninera", "child") ->
            material(Icons.Outlined.ChildCare)
        matches(key, "deporte", "fitness", "gimnas") ->
            material(Icons.Outlined.FitnessCenter)
        matches(key, "foto", "camara", "video") ->
            material(Icons.Outlined.PhotoCamera)
        matches(key, "clima", "aire acond", "calefacc", "refriger") ->
            material(Icons.Outlined.AcUnit)
        matches(key, "mueble", "tapiz") -> material(Icons.Outlined.Chair)
        matches(key, "modista", "costur", "confeccion") ->
            material(Icons.Outlined.Checkroom)
        matches(key, "musica") || isShortToken(key, "dj") ->
            material(Icons.Outlined.MusicNote)
        matches(key, "jardin", "paisaj", "poda") -> material(Icons.Outlined.Yard)
        matches(key, "plaga", "fumig") -> material(Icons.Outlined.PestControl)
        matches(key, "techo", "techista") -> material(Icons.Outlined.Roofing)
        matches(key, "vidrio", "vidrier") -> material(Icons.Outlined.Window)
        matches(key, "desagote", "cloaca") -> material(Icons.Outlined.WaterDrop)
        matches(key, "bienestar", "yoga", "masaj") ->
            material(Icons.Outlined.SelfImprovement)
        matches(key, "alba") -> material(Icons.Outlined.Construction)
        matches(key, "ingen") -> material(Icons.Outlined.Engineering)
        matches(key, "instal") -> material(Icons.Outlined.HomeRepairService)
        matches(key, "manten", "refacc") -> material(Icons.Outlined.Handyman)

        else -> null
    }

    private fun hashPick(categoryId: Long, name: String): CategoryVectorIcon {
        val hash = normalize(name).hashCode() * 31 + categoryId.toInt()
        val index = hash.mod(UNKNOWN_ICONS.size)
        return material(UNKNOWN_ICONS[index])
    }

    private fun lucide(@DrawableRes resId: Int) = CategoryVectorIcon.Lucide(resId)

    private fun material(imageVector: ImageVector) = CategoryVectorIcon.Material(imageVector)

    private fun matches(key: String, vararg keywords: String): Boolean =
        keywords.any { key.contains(it) }

    private fun isShortToken(key: String, token: String): Boolean {
        if (key == token) return true
        return key.split(' ').any { part -> part == token }
    }

    private fun isGasTrade(key: String): Boolean {
        if (key.contains("gasista")) return true
        if (key.contains("gas natural")) return true
        return isShortToken(key, "gas")
    }

    private fun sanitizeUrl(iconUrl: String?): String? =
        iconUrl?.trim()?.takeIf { it.isNotEmpty() }

    private val ACCENT_MARKS = "\\p{M}+".toRegex()
    private val WHITESPACE = "\\s+".toRegex()

    /**
     * Set genérico sin martillo: dos ids consecutivos caen en iconos distintos
     * porque el índice avanza de a 1 modulo el tamaño.
     */
    private val UNKNOWN_ICONS = listOf(
        Icons.Outlined.MiscellaneousServices,
        Icons.Outlined.HomeRepairService,
        Icons.Outlined.Handyman,
        Icons.Outlined.WorkOutline,
        Icons.Outlined.Category,
        Icons.Outlined.HomeWork,
        Icons.Outlined.Engineering,
        Icons.Outlined.Architecture,
    )
}
