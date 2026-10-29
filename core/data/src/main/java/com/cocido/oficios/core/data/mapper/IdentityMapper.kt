package com.cocido.oficios.core.data.mapper

import com.cocido.oficios.core.network.api.profile.dto.CertificadoTrabajoDto
import com.cocido.oficios.domain.model.ProviderDniDocuments
import com.cocido.oficios.domain.model.ProviderSelfieDocument
import com.cocido.oficios.domain.model.WorkCertificateInfo
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

fun CertificadoTrabajoDto.toDomain() = WorkCertificateInfo(
    id = id,
    name = nombre,
    fileUrl = urlArchivo,
)

fun JsonElement.toProviderDniDocuments(): ProviderDniDocuments? {
    val document = findDocumentByType("dni") ?: return null
    return ProviderDniDocuments(
        frontUrl = document.stringOrNull("frenteUrl"),
        backUrl = document.stringOrNull("reversoUrl"),
        frontStatus = document.stringOrNull("frenteEstado"),
        backStatus = document.stringOrNull("reversoEstado"),
        frontFileName = document.stringOrNull("frenteNombreArchivo"),
        backFileName = document.stringOrNull("reversoNombreArchivo"),
    )
}

fun JsonElement.toProviderSelfieDocument(): ProviderSelfieDocument? {
    val document = findDocumentByType("selfie") ?: return null
    return ProviderSelfieDocument(
        id = document.longOrNull("id"),
        url = document.stringOrNull("frenteUrl") ?: document.stringOrNull("selfieUrl"),
        status = document.stringOrNull("frenteEstado") ?: document.stringOrNull("selfieEstado"),
        rejectionReason = document.stringOrNull("motivoRechazo"),
    )
}

private fun JsonElement.findDocumentByType(tipoDocumento: String): JsonObject? {
    val root = this as? JsonObject ?: return null
    val data = root["data"]?.jsonArray ?: return null
    val firstGroup = data.firstOrNull()?.jsonObject ?: return null
    val documents = firstGroup["documentos"]?.jsonArray
    
    if (documents != null && documents.isNotEmpty()) {
        return documents
            .mapNotNull { it.jsonObject }
            .firstOrNull { doc ->
                doc.stringOrNull("tipoDocumento")?.equals(tipoDocumento, ignoreCase = true) == true
            }
    }
    
    val groupType = firstGroup.stringOrNull("tipoDocumento")
    if (groupType?.equals(tipoDocumento, ignoreCase = true) == true) {
        return firstGroup
    }
    
    return null
}

fun JsonElement.toWorkCertificateList(): List<WorkCertificateInfo> {
    val root = this as? JsonObject ?: return emptyList()
    val data = root["data"]?.jsonArray ?: return emptyList()
    return data.mapNotNull { element ->
        val item = element.jsonObject
        val id = item["id"]?.jsonPrimitive?.longOrNull ?: return@mapNotNull null
        val name = item.stringOrNull("nombre") ?: return@mapNotNull null
        WorkCertificateInfo(
            id = id,
            name = name,
            fileUrl = item.stringOrNull("urlArchivo"),
        )
    }
}

private fun JsonObject.stringOrNull(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull

private fun JsonObject.longOrNull(key: String): Long? =
    this[key]?.jsonPrimitive?.longOrNull
