package com.cocido.oficios.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.cocido.oficios.core.common.result.AppError
import com.cocido.oficios.core.common.result.AppResult
import com.cocido.oficios.domain.model.FormosaAddressGeocoding
import com.cocido.oficios.domain.model.GeocodedPlace
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
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class AndroidLocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
) : LocationRepository {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(requirePrecise: Boolean): AppResult<GeoPoint> {
        permissionError(requirePrecise)?.let { return AppResult.Error(it) }
        if (!isLocationEnabled()) {
            return AppResult.Error(AppError.Validation("Active la ubicación del dispositivo para continuar."))
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
                AppResult.Error(AppError.Network("No pudimos obtener su ubicación. Compruebe la señal e intente nuevamente."))
            } else {
                AppResult.Success(location.toGeoPoint())
            }
        } catch (_: SecurityException) {
            AppResult.Error(AppError.Forbidden("Necesitamos permiso de ubicación para continuar."))
        } catch (_: Exception) {
            AppResult.Error(AppError.Network("No pudimos obtener su ubicación. Intente nuevamente."))
        }
    }

    override suspend fun geocodeForward(query: String): AppResult<List<GeocodedPlace>> {
        if (!Geocoder.isPresent()) return AppResult.Success(emptyList())
        return try {
            val addresses = withContext(Dispatchers.IO) { lookupAddresses(query) }
            AppResult.Success(addresses.mapNotNull { it.toGeocodedPlace() })
        } catch (_: Exception) {
            AppResult.Error(AppError.Network(FormosaAddressGeocoding.GEOCODER_ERROR_MESSAGE))
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
            trySend(AppResult.Error(AppError.Validation("Active la ubicación del dispositivo para compartir su recorrido.")))
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
            trySend(AppResult.Error(AppError.Network("No pudimos actualizar su ubicación.")))
            close()
        }
        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    private fun permissionError(requirePrecise: Boolean): AppError? {
        val hasFine = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarse = hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        return when {
            requirePrecise && !hasFine -> AppError.Forbidden(
                "Permita la ubicación precisa para compartir su recorrido de forma segura.",
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

    private suspend fun lookupAddresses(query: String): List<Address> {
        val geocoder = Geocoder(context, Locale("es", "AR"))
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocationName(
                    query,
                    MAX_GEOCODE_RESULTS,
                    FormosaAddressGeocoding.SOUTH,
                    FormosaAddressGeocoding.WEST,
                    FormosaAddressGeocoding.NORTH,
                    FormosaAddressGeocoding.EAST,
                ) { addresses ->
                    if (continuation.isActive) continuation.resume(addresses)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocationName(
                query,
                MAX_GEOCODE_RESULTS,
                FormosaAddressGeocoding.SOUTH,
                FormosaAddressGeocoding.WEST,
                FormosaAddressGeocoding.NORTH,
                FormosaAddressGeocoding.EAST,
            ).orEmpty()
        }
    }

    private fun Address.toGeocodedPlace(): GeocodedPlace? {
        if (!hasLatitude() || !hasLongitude()) return null
        return GeocodedPlace(
            latitude = latitude,
            longitude = longitude,
            formattedAddress = listOfNotNull(
                thoroughfare,
                subThoroughfare,
                locality,
                adminArea,
                countryName,
            ).joinToString(", "),
            locality = locality,
            subAdminArea = subAdminArea,
            adminArea = adminArea,
            countryName = countryName,
        )
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
        const val MAX_GEOCODE_RESULTS = 5
    }
}
