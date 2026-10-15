package com.cocido.oficios.core.designsystem.component.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.cocido.oficios.core.designsystem.theme.OficiosComponentSize
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
        val content: @Composable () -> Unit,
    )

    private val cases = listOf(
        FieldCase("vacio_con_label") {
            OficiosTextField(value = "", onValueChange = {}, label = "Correo", modifier = Modifier.fillMaxWidth())
        },
        FieldCase("label_flotante") {
            OficiosTextField(value = "usuario@mail.com", onValueChange = {}, label = "Correo", modifier = Modifier.fillMaxWidth())
        },
        FieldCase("correo_largo") {
            OficiosTextField(
                value = "nombre.apellido.muy.largo+pruebas@dominio-extenso.com.ar",
                onValueChange = {},
                label = "Correo",
                modifier = Modifier.fillMaxWidth(),
            )
        },
        FieldCase("solo_placeholder") {
            OficiosTextField(value = "Juan García", onValueChange = {}, label = "", placeholder = "Nombre completo", modifier = Modifier.fillMaxWidth())
        },
        FieldCase("contrasena_con_ojo") {
            OficiosPasswordField(value = "secreto123", onValueChange = {}, label = "Contraseña", modifier = Modifier.fillMaxWidth())
        },
        FieldCase("error_con_soporte") {
            OficiosTextField(
                value = "mal@",
                onValueChange = {},
                label = "Correo",
                isError = true,
                supportingText = "El email es obligatorio",
                modifier = Modifier.fillMaxWidth(),
            )
        },
        FieldCase("trailing_icon") {
            OficiosTextField(
                value = "Plomería",
                onValueChange = {},
                label = "Rubro",
                modifier = Modifier.fillMaxWidth(),
                trailingContent = { Icon(Icons.Default.Search, contentDescription = null) },
            )
        },
        FieldCase("multilinea") {
            OficiosTextField(
                value = "Primera línea\nSegunda línea",
                onValueChange = {},
                label = "Comentario",
                singleLine = false,
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )

    @Test
    fun alturaMinima56dp_fuente100() = assertMinFieldHeight(fontScale = 1.0f)

    @Test
    fun alturaMinimaMayorQue56dp_fuente200() = assertMinFieldHeight(fontScale = 2.0f, greaterThanBase = true)

    @Test
    fun textNoQuedaRecortado_fuente100() = assertTextFits(fontScale = 1.0f)

    @Test
    fun textNoQuedaRecortado_fuente130() = assertTextFits(fontScale = 1.3f)

    @Test
    fun textNoQuedaRecortado_fuente200() = assertTextFits(fontScale = 2.0f)

    private fun assertMinFieldHeight(fontScale: Float, greaterThanBase: Boolean = false) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                OficiosTheme {
                    Box(Modifier.testTag("altura")) {
                        OficiosTextField(
                            value = "ab",
                            onValueChange = {},
                            label = "Nombre",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        val field = composeTestRule.onNodeWithTag("altura", useUnmergedTree = true).fetchSemanticsNode()
        val minPx = with(composeTestRule.density) { OficiosComponentSize.textFieldHeight.toPx() * fontScale }
        assertTrue(
            "altura @${fontScale}x: ${field.size.height}px < mínimo ${minPx}px",
            field.size.height + 0.5f >= minPx,
        )
        if (greaterThanBase) {
            val basePx = with(composeTestRule.density) { OficiosComponentSize.textFieldHeight.toPx() }
            assertTrue(
                "altura @2.0x debería superar 56dp base",
                field.size.height > basePx + 1f,
            )
        }
    }

    private fun assertTextFits(fontScale: Float) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                OficiosTheme {
                    Column(
                        modifier = Modifier.width(360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        cases.forEach { case ->
                            Box(Modifier.testTag(case.tag)) {
                                case.content()
                            }
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        cases.forEach { case ->
            val field = composeTestRule.onNodeWithTag(case.tag, useUnmergedTree = true).fetchSemanticsNode()
            val valueLayout = field.findEditableTextLayout()
                ?: error("${case.tag}: no se encontró el layout del valor")
            assertTrue(
                "${case.tag} @${fontScale}x: el valor queda recortado en alto",
                !valueLayout.didOverflowHeight,
            )
            assertTrue(
                "${case.tag} @${fontScale}x: lineHeight menor que fontSize",
                valueLayout.layoutInput.style.lineHeight.value >= valueLayout.layoutInput.style.fontSize.value,
            )
        }
    }

    private fun SemanticsNode.findEditableTextLayout(): TextLayoutResult? {
        if (config.getOrNull(SemanticsActions.SetText) != null) {
            config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.let { action ->
                val results = mutableListOf<TextLayoutResult>()
                if (action(results) && results.isNotEmpty()) return results.first()
            }
        }
        return children.firstNotNullOfOrNull { it.findEditableTextLayout() }
    }
}
