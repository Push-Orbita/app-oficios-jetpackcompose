package com.cocido.oficios.core.data.mapper

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IdentityMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `toProviderSelfieDocument returns null when documentos is empty`() {
        val response = """
        {
            "metadata": { "count": 1 },
            "data": [
                {
                    "id": 11,
                    "trabajadorOficioId": 11,
                    "documentos": []
                }
            ]
        }
        """.trimIndent()

        val element = json.parseToJsonElement(response)
        val result = element.toProviderSelfieDocument()

        assertNull("Should return null when documentos array is empty", result)
    }

    @Test
    fun `toProviderSelfieDocument maps from frenteUrl when selfieUrl is absent`() {
        val response = """
        {
            "metadata": { "count": 1 },
            "data": [
                {
                    "id": 11,
                    "trabajadorOficioId": 11,
                    "documentos": [
                        {
                            "id": 16,
                            "tipoDocumento": "selfie",
                            "frenteUrl": "https://example.com/selfie.jpg",
                            "frenteEstado": "APROBADO",
                            "motivoRechazo": null
                        }
                    ]
                }
            ]
        }
        """.trimIndent()

        val element = json.parseToJsonElement(response)
        val result = element.toProviderSelfieDocument()

        assertEquals(16L, result?.id)
        assertEquals("https://example.com/selfie.jpg", result?.url)
        assertEquals("APROBADO", result?.status)
        assertNull(result?.rejectionReason)
    }

    @Test
    fun `toProviderSelfieDocument prefers selfieUrl when both fields exist`() {
        val response = """
        {
            "metadata": { "count": 1 },
            "data": [
                {
                    "id": 11,
                    "trabajadorOficioId": 11,
                    "documentos": [
                        {
                            "id": 16,
                            "tipoDocumento": "selfie",
                            "frenteUrl": "https://example.com/frente.jpg",
                            "selfieUrl": "https://example.com/selfie.jpg",
                            "frenteEstado": "RECHAZADO",
                            "selfieEstado": "APROBADO",
                            "motivoRechazo": null
                        }
                    ]
                }
            ]
        }
        """.trimIndent()

        val element = json.parseToJsonElement(response)
        val result = element.toProviderSelfieDocument()

        assertEquals("https://example.com/frente.jpg", result?.url)
        assertEquals("RECHAZADO", result?.status)
    }

    @Test
    fun `toProviderSelfieDocument picks correct document when multiple types exist`() {
        val response = """
        {
            "metadata": { "count": 1 },
            "data": [
                {
                    "id": 11,
                    "trabajadorOficioId": 11,
                    "documentos": [
                        {
                            "id": 14,
                            "tipoDocumento": "dni",
                            "frenteUrl": "https://example.com/dni-front.jpg",
                            "reversoUrl": "https://example.com/dni-back.jpg",
                            "frenteEstado": "APROBADO",
                            "reversoEstado": "APROBADO"
                        },
                        {
                            "id": 16,
                            "tipoDocumento": "selfie",
                            "frenteUrl": "https://example.com/selfie.jpg",
                            "frenteEstado": "RECHAZADO",
                            "motivoRechazo": "Foto borrosa"
                        }
                    ]
                }
            ]
        }
        """.trimIndent()

        val element = json.parseToJsonElement(response)
        val result = element.toProviderSelfieDocument()

        assertEquals(16L, result?.id)
        assertEquals("https://example.com/selfie.jpg", result?.url)
        assertEquals("RECHAZADO", result?.status)
        assertEquals("Foto borrosa", result?.rejectionReason)
    }

    @Test
    fun `toProviderSelfieDocument returns null when no selfie document exists`() {
        val response = """
        {
            "metadata": { "count": 1 },
            "data": [
                {
                    "id": 11,
                    "trabajadorOficioId": 11,
                    "documentos": [
                        {
                            "id": 14,
                            "tipoDocumento": "dni",
                            "frenteUrl": "https://example.com/dni-front.jpg",
                            "reversoUrl": "https://example.com/dni-back.jpg",
                            "frenteEstado": "APROBADO",
                            "reversoEstado": "APROBADO"
                        }
                    ]
                }
            ]
        }
        """.trimIndent()

        val element = json.parseToJsonElement(response)
        val result = element.toProviderSelfieDocument()

        assertNull("Should return null when no selfie document exists", result)
    }

    @Test
    fun `toProviderDniDocuments maps correctly from documentos array`() {
        val response = """
        {
            "metadata": { "count": 1 },
            "data": [
                {
                    "id": 11,
                    "trabajadorOficioId": 11,
                    "documentos": [
                        {
                            "id": 14,
                            "tipoDocumento": "dni",
                            "frenteUrl": "https://example.com/dni-front.jpg",
                            "reversoUrl": "https://example.com/dni-back.jpg",
                            "frenteEstado": "APROBADO",
                            "reversoEstado": "APROBADO",
                            "frenteNombreArchivo": "front.jpg",
                            "reversoNombreArchivo": "back.jpg"
                        }
                    ]
                }
            ]
        }
        """.trimIndent()

        val element = json.parseToJsonElement(response)
        val result = element.toProviderDniDocuments()

        assertEquals("https://example.com/dni-front.jpg", result?.frontUrl)
        assertEquals("https://example.com/dni-back.jpg", result?.backUrl)
        assertEquals("APROBADO", result?.frontStatus)
        assertEquals("APROBADO", result?.backStatus)
        assertEquals("front.jpg", result?.frontFileName)
        assertEquals("back.jpg", result?.backFileName)
    }

    @Test
    fun `toProviderDniDocuments returns null when no dni document exists`() {
        val response = """
        {
            "metadata": { "count": 1 },
            "data": [
                {
                    "id": 11,
                    "trabajadorOficioId": 11,
                    "documentos": [
                        {
                            "id": 16,
                            "tipoDocumento": "selfie",
                            "frenteUrl": "https://example.com/selfie.jpg",
                            "frenteEstado": "APROBADO"
                        }
                    ]
                }
            ]
        }
        """.trimIndent()

        val element = json.parseToJsonElement(response)
        val result = element.toProviderDniDocuments()

        assertNull("Should return null when no dni document exists", result)
    }

    @Test
    fun `toProviderSelfieDocument handles case-insensitive tipoDocumento matching`() {
        val response = """
        {
            "metadata": { "count": 1 },
            "data": [
                {
                    "id": 11,
                    "trabajadorOficioId": 11,
                    "documentos": [
                        {
                            "id": 16,
                            "tipoDocumento": "SELFIE",
                            "frenteUrl": "https://example.com/selfie.jpg",
                            "frenteEstado": "SUBIDO"
                        }
                    ]
                }
            ]
        }
        """.trimIndent()

        val element = json.parseToJsonElement(response)
        val result = element.toProviderSelfieDocument()

        assertEquals(16L, result?.id)
        assertEquals("https://example.com/selfie.jpg", result?.url)
        assertEquals("SUBIDO", result?.status)
    }
}
