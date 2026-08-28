package com.cocido.oficios.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.GeoPoint
import com.cocido.oficios.domain.repository.LocationRepository
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class AndroidLocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
) : LocationRepository {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(requirePrecise: Boolean): AppResult<GeoPoint> {
        permissionError(requirePrecise)?.let { return AppResult.Error(it) }
        if (!isLocationEnabled()) {
            return AppResult.Error(AppError.Validation("Activá la ubicación del dispositivo para continuar."))
        }

        return try {
            val cancellation = CancellationTokenSource()
            val request = CurrentLocationRequest.Builder()
                .setPriority(if (requirePrecise) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setDurationMillis(CURRENT_LOCATION_TIMEOUT_MILLIS)
                .setMaxUpdateAgeMillis(MAX_CACHED_LOCATION_AGE_MILLIS)
                .build()
            val location = withTimeoutOrNull(CURRENT_LOCATION_TIMEOUT_MILLIS + 1_000L) {
                fusedLocationClient.getCurrentLocation(request, cancellation.token).await()
            }
            if (location == null) {
                AppResult.Error(AppError.Network("No pudimos obtener tu ubicación. Comprobá la señal e intentá nuevamente."))
            } else {
                AppResult.Success(location.toGeoPoint())
            }
        } catch (_: SecurityException) {
            AppResult.Error(AppError.Forbidden("Necesitamos permiso de ubicación para continuar."))
        } catch (_: Exception) {
            AppResult.Error(AppError.Network("No pudimos obtener tu ubicación. Intentá nuevamente."))
        }
    }

    @SuppressLint("MissingPermission")
    override fun observeLocations(intervalMillis: Long): Flow<AppResult<GeoPoint>> = callbackFlow {
        permissionError(requirePrecise = true)?.let {
            trySend(AppResult.Error(it))
            close()
            return@callbackFlow
        }
        if (!isLocationEnabled()) {
            trySend(AppResult.Error(AppError.Validation("Activá la ubicación del dispositivo para compartir tu recorrido.")))
            close()
            return@callbackFlow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .setMinUpdateIntervalMillis((intervalMillis / 2).coerceAtLeast(5_000L))
            .setMaxUpdateDelayMillis(intervalMillis)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(AppResult.Success(it.toGeoPoint())) }
            }
        }
        try {
            fusedLocationClient.requestLocationUpdates(request, callback, context.mainLooper)
        } catch (_: SecurityException) {
            trySend(AppResult.Error(AppError.Forbidden("Se quitó el permiso de ubicación durante el recorrido.")))
            close()
        } catch (_: Exception) {
            trySend(AppResult.Error(AppError.Network("No pudimos actualizar tu ubicación.")))
            close()
        }
        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    private fun permissionError(requirePrecise: Boolean): AppError? {
        val hasFine = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarse = hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        return when {
            requirePrecise && !hasFine -> AppError.Forbidden(
                "Permití la ubicación precisa para compartir tu recorrido de forma segura.",
            )
            !hasFine && !hasCoarse -> AppError.Forbidden("Necesitamos permiso de ubicación para continuar.")
            else -> null
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun isLocationEnabled(): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            manager.isLocationEnabled
        } else {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    private fun Location.toGeoPoint() = GeoPoint(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracy,
        capturedAtEpochMillis = time,
    )

    private companion object {
        const val CURRENT_LOCATION_TIMEOUT_MILLIS = 15_000L
        const val MAX_CACHED_LOCATION_AGE_MILLIS = 30_000L
    }
}
