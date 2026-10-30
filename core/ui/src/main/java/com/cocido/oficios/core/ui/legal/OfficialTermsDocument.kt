package com.cocido.oficios.core.ui.legal

import android.content.Context
import java.nio.charset.StandardCharsets

/**
 * Bases y Condiciones oficiales de OFICIOS FORMOSA (texto del DOCX de Matías).
 * Fuente embebida: assets/legal/oficios_formosa_bases_y_condiciones.txt
 */
internal object OfficialTermsDocument {

    private const val ASSET_PATH = "legal/oficios_formosa_bases_y_condiciones.txt"

    /** Títulos/secciones del documento oficial (tal cual, sin inventar). */
    private val sectionHeadings = setOf(
        "Declaración Jurada y Aceptación",
        "INFORMACIÓN LEGAL",
        "INFORMACIÓN TÉCNICA",
        "TÉRMINOS Y CONDICIONES DE USO GENERAL",
        "Condiciones Generales",
        "Registros de usuarios",
        "Protección de Datos",
        "Gratuidad y no exigencia de suscripción",
        "Veracidad y actualización de los datos",
        "Correcta utilización de las aplicaciones",
        "Mayoría de edad",
        "Comunicaciones comerciales",
        "Propiedad intelectual (derechos de autor, propiedad industrial y equivalentes)",
        "Usos indebidos",
        "CONDICIONES PARTICULARES DE OFICIOS FORMOSA (OFICIOS FORMOSA Y USUARIOS)",
        "CONDICIONES QUE RIGEN EL CONTRATO: OFICIOS FORMOSA Y EL TRABAJADOR PROFESIONAL (QUIEN TAMBIÉN PODRÁ DENOMINARSE “EL USUARIO”)",
        "TÉRMINOS Y CONDICIONES DE PAGO",
        "PRECIO",
        "LIMITACIÓN DE RESPONSABILIDAD.",
    )

    fun loadParagraphs(context: Context): List<TermsParagraph> {
        val raw = context.assets.open(ASSET_PATH).use { input ->
            input.bufferedReader(StandardCharsets.UTF_8).readText()
        }
        return parse(raw)
    }

    internal fun parse(raw: String): List<TermsParagraph> =
        raw
            .lineSequence()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
            .map { line ->
                if (line in sectionHeadings) {
                    TermsParagraph.Heading(line)
                } else {
                    TermsParagraph.Body(line)
                }
            }
            .toList()
}

internal sealed interface TermsParagraph {
    val text: String

    data class Heading(override val text: String) : TermsParagraph
    data class Body(override val text: String) : TermsParagraph
}
