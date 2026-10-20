package com.cocido.oficios.core.network.api.worker

import com.cocido.oficios.core.network.api.worker.dto.TrabajadorOficioPerfilCompletoDto
import retrofit2.http.GET
import retrofit2.http.Path

interface WorkerProfileApi {

    @GET("trabajador-oficio/{id}")
    suspend fun getWorkerProfile(
        @Path("id") id: Long,
    ): TrabajadorOficioPerfilCompletoDto
}

