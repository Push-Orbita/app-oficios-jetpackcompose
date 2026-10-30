package com.cocido.oficios.core.ui.legal

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfficialTermsDocumentTest {

    @Test
    fun `official asset has no draft placeholder copy and keeps section headings`() {
        val asset = File("src/main/res/raw/oficios_formosa_bases_y_condiciones.txt")
        assertTrue("falta el raw oficial: ${asset.absolutePath}", asset.isFile)

        val raw = asset.readText(Charsets.UTF_8)
        assertFalse(raw.contains("versión definitiva", ignoreCase = true))
        assertFalse(raw.contains("Este texto resume", ignoreCase = true))
        assertFalse(raw.contains("lorem", ignoreCase = true))
        assertTrue(raw.contains("OFICIOS FORMOSA"))
        assertTrue(raw.contains("Asociación de Emprendedores Formoseños"))

        val paragraphs = OfficialTermsDocument.parse(raw)
        assertTrue(paragraphs.size > 50)
        assertTrue(paragraphs.any { it is TermsParagraph.Heading && it.text == "INFORMACIÓN LEGAL" })
        assertTrue(paragraphs.any { it is TermsParagraph.Heading && it.text == "TÉRMINOS Y CONDICIONES DE PAGO" })
        assertTrue(paragraphs.any { it is TermsParagraph.Heading && it.text.startsWith("LIMITACIÓN DE RESPONSABILIDAD") })
        assertEquals(
            paragraphs.count { it is TermsParagraph.Heading },
            paragraphs.filterIsInstance<TermsParagraph.Heading>().distinctBy { it.text }.size,
        )
    }
}
