package com.example.appnombre_grupo1.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

data class Ubicacion(
    val latitud: Double,
    val longitud: Double,
)

object UbicacionUtils {

    // Rango máximo permitido en metros (100 metros)
    const val RANGO_MAXIMO_METROS = 100.0f

    /**
     * Calcula la distancia geodésica en metros entre dos coordenadas (WGS84).
     */
    fun calcularDistanciaMetros(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Float {
        val resultados = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, resultados)
        return resultados[0]
    }

    /**
     * Obtiene la ubicación GPS actual usando LocationManager nativo de Android.
     */
    @SuppressLint("MissingPermission")
    fun obtenerUbicacionActual(
        context: Context,
        onUbicacionObtenida: (Ubicacion?, String?) -> Unit,
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onUbicacionObtenida(null, "Servicio de ubicación no disponible")
            return
        }

        val gpsHabilitado = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val redHabilitada = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!gpsHabilitado && !redHabilitada) {
            onUbicacionObtenida(null, "El GPS está desactivado en el dispositivo")
            return
        }

        val ultimaGps = try { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: Exception) { null }
        val ultimaRed = try { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { null }

        val mejorUltima = when {
            ultimaGps != null && ultimaRed != null -> if (ultimaGps.time > ultimaRed.time) ultimaGps else ultimaRed
            ultimaGps != null -> ultimaGps
            else -> ultimaRed
        }

        if (mejorUltima != null && (System.currentTimeMillis() - mejorUltima.time) < 60_000) {
            onUbicacionObtenida(Ubicacion(mejorUltima.latitude, mejorUltima.longitude), null)
            return
        }

        val provider = if (gpsHabilitado) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                onUbicacionObtenida(Ubicacion(location.latitude, location.longitude), null)
            }
            @Deprecated("Deprecated in API 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager.requestSingleUpdate(provider, listener, null)
            if (mejorUltima != null) {
                onUbicacionObtenida(Ubicacion(mejorUltima.latitude, mejorUltima.longitude), null)
            }
        } catch (e: SecurityException) {
            onUbicacionObtenida(null, "Permisos de ubicación no concedidos")
        } catch (e: Exception) {
            if (mejorUltima != null) {
                onUbicacionObtenida(Ubicacion(mejorUltima.latitude, mejorUltima.longitude), null)
            } else {
                onUbicacionObtenida(null, "No se pudo obtener la ubicación GPS")
            }
        }
    }
}
