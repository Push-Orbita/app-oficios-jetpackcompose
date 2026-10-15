package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.cocido.oficios.core.designsystem.theme.OficiosTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h3000dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OficiosTextFieldHeightTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private data class FieldCase(
        val tag: String,
        val hasLabel: Boolean,
        val supportingText: String? = null,
        val content: @Composable (Modifier) -> Unit,
    )

    private val cases = listOf(
        FieldCase("vacio_con_label", hasLabel = true) {
            OficiosTextField(value = "", onValueChange = {}, label = "Correo", modifier = it)
        },
        FieldCase("label_flotante", hasLabel = true) {
            OficiosTextField(value = "usuario@mail.com", onValueChange = {}, label = "Correo", modifier = it)
        },
        FieldCase("correo_largo", hasLabel = true) {
            OficiosTextField(
                value = "nombre.apellido.muy.largo+pruebas@dominio-extenso.com.ar",
                onValueChange = {},
                label = "Correo",
                modifier = it,
            )
        },
        FieldCase("solo_placeholder", hasLabel = false) {
            OficiosTextField(value = "Juan García", onValueChange = {}, label = "", placeholder = "Nombre completo", modifier = it)
        },
        FieldCase("contrasena_con_ojo", hasLabel = true) {
            OficiosPasswordField(value = "secreto123", onValueChange = {}, label = "Contraseña", modifier = it)
        },
        FieldCase("error_con_soporte", hasLabel = true, supportingText = "El email es obligatorio") {
            OficiosTextField(
                value = "mal@",
                onValueChange = {},
                label = "Correo",
                isError = true,
                supportingText = "El email es obligatorio",
                modifier = it,
            )
        },
        FieldCase("trailing_icon", hasLabel = true) {
            OficiosTextField(
                value = "Plomería",
                onValueChange = {},
                label = "Rubro",
                modifier = it,
                trailingContent = { Icon(Icons.Default.Search, contentDescription = null) },
            )
        },
        FieldCase("multilinea", hasLabel = true) {
            OficiosTextField(
                value = "Primera línea\nSegunda línea",
                onValueChange = {},
                label = "Comentario",
                singleLine = false,
                minLines = 3,
                maxLines = 6,
                modifier = it,
            )
        },
    )

    @Test
    fun textNoQuedaRecortado_fuente100() = assertTextFits(fontScale = 1.0f)

    @Test
    fun textNoQuedaRecortado_fuente130() = assertTextFits(fontScale = 1.3f)

    @Test
    fun textNoQuedaRecortado_fuente200() = assertTextFits(fontScale = 2.0f)

    private fun assertTextFits(fontScale: Float) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                OficiosTheme {
                    Column(
                        modifier = Modifier.width(360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        cases.forEach { case -> case.content(Modifier.testTag(case.tag)) }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        val density = composeTestRule.density
        cases.forEach { case ->
            val field = composeTestRule.onNodeWithTag(case.tag, useUnmergedTree = true).fetchSemanticsNode()
            val textLayout = field.findTextLayout()
                ?: error("${case.tag}: no se encontró el layout del texto")
            val fieldHeight = field.size.height.toFloat()
            val labelTop = if (case.hasLabel) with(density) { 8.dp.toPx() } else 0f
            val verticalPadding = with(density) { (16.dp * 2).toPx() }
            val supportingHeight = case.supportingText?.let { text ->
                val node = composeTestRule.onNodeWithText(text, useUnmergedTree = true).fetchSemanticsNode()
                node.size.height + with(density) { 4.dp.toPx() }
            } ?: 0f
            // multiParagraph.height es el alto real de las líneas; size.height se achica si el campo no le deja lugar.
            val textHeight = maxOf(textLayout.size.height.toFloat(), textLayout.multiParagraph.height)
            val required = labelTop + verticalPadding + textHeight + supportingHeight

            assertTrue(
                "${case.tag} @${fontScale}x: campo ${fieldHeight}px < texto+padding ${required}px",
                fieldHeight + 0.5f >= required,
            )
            assertTrue(
                "${case.tag} @${fontScale}x: lineHeight menor que fontSize",
                textLayout.layoutInput.style.lineHeight.value >= textLayout.layoutInput.style.fontSize.value,
            )
        }
    }

    private fun SemanticsNode.findTextLayout(): TextLayoutResult? {
        config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.let { action ->
            val results = mutableListOf<TextLayoutResult>()
            if (action(results) && results.isNotEmpty()) return results.first()
        }
        return children.firstNotNullOfOrNull { it.findTextLayout() }
    }
}
