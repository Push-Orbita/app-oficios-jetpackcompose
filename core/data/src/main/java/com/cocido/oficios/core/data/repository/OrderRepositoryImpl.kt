package com.cocido.oficios.core.data.repository

import com.cocido.oficios.core.common.dispatcher.DispatcherProvider
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.map
import com.cocido.oficios.core.data.mapper.toDomain
import com.cocido.oficios.core.data.mapper.toListItem
import com.cocido.oficios.core.data.remote.OrderRemoteDataSource
import com.cocido.oficios.core.network.api.orders.dto.CreateSolicitudServicioRequestDto
import com.cocido.oficios.domain.model.CreateServiceRequestParams
import com.cocido.oficios.domain.model.ServiceOrder
import com.cocido.oficios.domain.model.ServiceOrderListItem
import com.cocido.oficios.domain.model.BookingTracking
import com.cocido.oficios.domain.model.GeoPoint
import com.cocido.oficios.domain.repository.OrderRepository
import javax.inject.Inject
import kotlinx.coroutines.withContext

class OrderRepositoryImpl @Inject constructor(
    private val remoteDataSource: OrderRemoteDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : OrderRepository {

    override suspend fun createServiceRequest(params: CreateServiceRequestParams): AppResult<Long> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.createSolicitud(
                CreateSolicitudServicioRequestDto(
                    trabajadorOficioSubRubroId = params.trabajadorOficioSubRubroId,
                    domicilioId = params.domicilioId,
                    contactoId = params.contactoId,
                    fechaPropuestaInicio = params.proposedStartIso,
                    fechaPropuestaFin = params.proposedEndIso,
                    descripcion = params.description,
                ),
            ).map { it.id }
        }

    override suspend fun getClientOrders(): AppResult<List<ServiceOrderListItem>> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.getMisSolicitudesCliente().map { response ->
                response.data.map { it.toListItem(isWorkerViewing = false) }
            }
        }

    override suspend fun getWorkerOrders(): AppResult<List<ServiceOrderListItem>> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.getSolicitudesTrabajador().map { response ->
                response.solicitudes.map { it.toListItem(isWorkerViewing = true) }
            }
        }

    override suspend fun getOrderDetail(orderId: Long): AppResult<ServiceOrder> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.getSolicitud(orderId).map { it.toDomain() }
        }

    override suspend fun acceptOrder(orderId: Long): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.aceptarSolicitud(orderId).map { }
        }

    override suspend fun rejectOrder(orderId: Long, reason: String): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.rechazarSolicitud(orderId, reason).map { }
        }

    override suspend fun sendCounterOffer(
        orderId: Long,
        newStartIso: String,
        newEndIso: String,
        newPrice: Double?,
    ): AppResult<Unit> = withContext(dispatcherProvider.io) {
        remoteDataSource.enviarContrapropuesta(orderId, newStartIso, newEndIso, newPrice).map { }
    }

    override suspend fun acceptCounterOffer(counterOfferId: Long): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.aceptarContrapropuesta(counterOfferId).map { }
        }

    override suspend fun rejectCounterOffer(counterOfferId: Long): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.rechazarContrapropuesta(counterOfferId).map { }
        }

    override suspend fun cancelOrderBeforeAcceptance(orderId: Long): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.cancelarSolicitudPendiente(orderId).map { }
        }

    override suspend fun getBookingTracking(bookingId: Long): AppResult<BookingTracking?> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.getTracking(bookingId).map { it?.toDomain() }
        }

    override suspend fun markEnRoute(bookingId: Long, location: GeoPoint): AppResult<Unit> {
        if (!location.isValid) return AppResult.Error(AppError.Validation("No pudimos obtener una ubicación válida."))
        return withContext(dispatcherProvider.io) {
            remoteDataSource.marcarTracking(
                bookingId,
                "EN_CAMINO",
                location.latitude.toString(),
                location.longitude.toString(),
            ).map { }
        }
    }

    override suspend fun markArrived(bookingId: Long, location: GeoPoint): AppResult<Unit> {
        if (!location.isValid) return AppResult.Error(AppError.Validation("No pudimos obtener una ubicación válida."))
        return withContext(dispatcherProvider.io) {
            remoteDataSource.marcarTracking(
                bookingId,
                "LLEGUE",
                location.latitude.toString(),
                location.longitude.toString(),
            ).map { }
        }
    }

    override suspend fun finalizeBooking(bookingId: Long): AppResult<Unit> =
        withContext(dispatcherProvider.io) {
            remoteDataSource.finalizarBooking(bookingId).map { }
        }

    override suspend fun submitRating(
        bookingId: Long,
        quality: Int,
        punctuality: Int,
        comment: String?,
    ): AppResult<Unit> = withContext(dispatcherProvider.io) {
        remoteDataSource.crearCalificacion(bookingId, quality, punctuality, comment).map { }
    }
}
