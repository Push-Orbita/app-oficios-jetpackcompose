package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.safety.ReportsApi
import com.cocido.oficios.core.network.api.safety.dto.CreateDenunciaRequestDto
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportsRemoteDataSource @Inject constructor(
    private val reportsApi: ReportsApi,
) {
    suspend fun getReasons() = safeApiCall {
        reportsApi.getMotivos()
    }

    suspend fun submit(
        workerId: Long,
        reasonCode: String,
        description: String,
        idempotencyKey: String,
        bookingId: Long?,
    ) = safeApiCall {
        reportsApi.create(
            idempotencyKey = idempotencyKey,
            request = CreateDenunciaRequestDto(
                targetType = "TRABAJADOR_OFICIO",
                targetId = workerId,
                reasonCode = reasonCode,
                description = description,
                bookingId = bookingId,
            ),
        )
    }
}
