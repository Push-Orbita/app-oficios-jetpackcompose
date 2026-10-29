package com.cocido.oficios.core.network.api.profile

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import com.cocido.oficios.core.common.contract.DniImageUploadContract

@Singleton
class IdentityDocumentUploadDataSource @Inject constructor(
    private val identityDocumentApi: IdentityDocumentApi,
) {
    suspend fun uploadDniImages(frontImagePath: String, backImagePath: String) {
        val frontFile = File(frontImagePath)
        val backFile = File(backImagePath)

        identityDocumentApi.uploadDniDocuments(
            frenteArchivo = createDniImagePart(
                fieldName = DniImageUploadContract.PART_FRONT_FIELD,
                fileNamePrefix = DniImageUploadContract.FRONT_FILE_PREFIX,
                file = frontFile,
            ),
            dorsoArchivo = createDniImagePart(
                fieldName = DniImageUploadContract.PART_BACK_FIELD,
                fileNamePrefix = DniImageUploadContract.BACK_FILE_PREFIX,
                file = backFile,
            ),
        )
    }

    suspend fun uploadSelfie(imagePath: String) {
        val file = File(imagePath)
        identityDocumentApi.uploadSelfie(createSelfieImagePart(file))
    }

    suspend fun replaceSelfie(documentId: Long, imagePath: String) {
        val file = File(imagePath)
        identityDocumentApi.replaceSelfie(
            documentId = documentId,
            selfieArchivo = createSelfieImagePart(file),
        )
    }
}
