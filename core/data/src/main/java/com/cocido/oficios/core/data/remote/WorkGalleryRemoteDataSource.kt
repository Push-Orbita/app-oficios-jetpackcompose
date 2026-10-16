package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.account.WorkGalleryApi
import com.cocido.oficios.core.network.api.account.WorkGalleryUploadDataSource
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkGalleryRemoteDataSource @Inject constructor(
    private val api: WorkGalleryApi,
    private val uploadDataSource: WorkGalleryUploadDataSource,
) {
    suspend fun search(workerId: Long) = safeApiCall { api.search(workerId) }
    suspend fun upload(filePaths: List<String>) = safeApiCall { uploadDataSource.upload(filePaths) }
    suspend fun delete(id: Long) = safeApiCall { api.delete(id) }
}
