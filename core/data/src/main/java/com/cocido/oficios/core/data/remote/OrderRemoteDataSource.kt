package com.cocido.oficios.core.data.remote

import com.cocido.oficios.core.network.api.orders.OrdersApi
import com.cocido.oficios.core.network.api.orders.dto.CancelarSolicitudRequestDto
import com.cocido.oficios.core.network.api.orders.dto.CreateCalificacionRequestDto
import com.cocido.oficios.core.network.api.orders.dto.CreateContrapropuestaRequestDto
import com.cocido.oficios.core.network.api.orders.dto.CreateSolicitudServicioRequestDto
import com.cocido.oficios.core.network.api.orders.dto.MarcarTrackingRequestDto
import com.cocido.oficios.core.network.api.orders.dto.RechazarSolicitudRequestDto
import com.cocido.oficios.core.network.api.orders.dto.UpdateSolicitudEstadoRequestDto
import com.cocido.oficios.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRemoteDataSource @Inject constructor(
    private val ordersApi: OrdersApi,
) {
    suspend fun createSolicitud(request: CreateSolicitudServicioRequestDto) = safeApiCall {
        ordersApi.createSolicitud(request)
    }

    suspend fun getMisSolicitudesCliente() = safeApiCall {
        ordersApi.getMisSolicitudesCliente()
    }

    suspend fun getSolicitudesTrabajador() = safeApiCall {
        ordersApi.getSolicitudesTrabajador()
    }

    suspend fun getSolicitud(id: Long) = safeApiCall {
        ordersApi.getSolicitud(id)
    }

    suspend fun aceptarSolicitud(id: Long) = safeApiCall {
        ordersApi.aceptarSolicitud(id)
    }

    suspend fun rechazarSolicitud(id: Long, motivo: String) = safeApiCall {
        ordersApi.rechazarSolicitud(id, RechazarSolicitudRequestDto(motivoRechazo = motivo))
    }

    suspend fun enviarContrapropuesta(id: Long, inicio: String, fin: String, precio: Double?) = safeApiCall {
        ordersApi.enviarContrapropuesta(
            id,
            CreateContrapropuestaRequestDto(fechaPropuestaInicio = inicio, fechaPropuestaFin = fin, nuevoPrecio = precio),
        )
    }

    suspend fun cancelarSolicitudPendiente(id: Long) = safeApiCall {
        ordersApi.cancelarSolicitudPendiente(id, UpdateSolicitudEstadoRequestDto(estado = "CANCELADA"))
    }

    suspend fun cancelarSolicitudConfirmada(id: Long, motivo: String?) = safeApiCall {
        ordersApi.cancelarSolicitudConfirmada(id, CancelarSolicitudRequestDto(motivoCancelacion = motivo))
    }

    suspend fun aceptarContrapropuesta(id: Long) = safeApiCall {
        ordersApi.aceptarContrapropuesta(id)
    }

    suspend fun rechazarContrapropuesta(id: Long) = safeApiCall {
        ordersApi.rechazarContrapropuesta(id)
    }

    suspend fun getBooking(id: Long) = safeApiCall {
        ordersApi.getBooking(id)
    }

    suspend fun finalizarBooking(id: Long) = safeApiCall {
        ordersApi.finalizarBooking(id)
    }

    suspend fun marcarTracking(bookingId: Long, estado: String, lat: String, lng: String) = safeApiCall {
        ordersApi.marcarTracking(bookingId, MarcarTrackingRequestDto(estado = estado, lat = lat, lng = lng))
    }

    suspend fun crearCalificacion(bookingId: Long, calidad: Int, puntualidad: Int, comentario: String?) = safeApiCall {
        ordersApi.crearCalificacion(
            CreateCalificacionRequestDto(bookingId = bookingId, calidad = calidad, puntualidad = puntualidad, comentario = comentario),
        )
    }
}
