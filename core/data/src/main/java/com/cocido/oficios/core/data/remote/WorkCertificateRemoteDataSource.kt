package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.profile.WorkCertificateApi
import com.cocido.oficios.core.network.api.profile.WorkCertificateUploadDataSource
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkCertificateRemoteDataSource @Inject constructor(
    private val workCertificateApi: WorkCertificateApi,
    private val workCertificateUploadDataSource: WorkCertificateUploadDataSource,
) {
    suspend fun uploadCertificate(name: String, filePath: String) = safeApiCall {
        workCertificateUploadDataSource.uploadCertificate(name, filePath)
    }

    suspend fun searchCertificates() = safeApiCall {
        workCertificateApi.searchCertificates()
    }
}
