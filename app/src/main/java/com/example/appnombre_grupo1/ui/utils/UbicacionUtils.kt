package com.example.appnombre_grupo1.ui.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat

data class Ubicacion(
    val latitud: Double,
    val longitud: Double,
)

object UbicacionUtils {

    // Rango máximo permitido en metros (100 metros)
    const val RANGO_MAXIMO_METROS = 100.0f

    // Tiempo máximo esperando una ubicación nueva antes de rendirse
    private const val TIEMPO_MAXIMO_MS = 15_000L

    // Antigüedad máxima de la última ubicación conocida para usarla como respaldo (2 minutos)
    private const val ANTIGUEDAD_MAXIMA_RESPALDO_MS = 120_000L

    /**
     * Verifica si la aplicación tiene concedidos los permisos de ubicación en runtime.
     */
    fun tienePermisoUbicacion(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

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

        // Si el teléfono tiene una ubicación de hace menos de 1 minuto, se usa directamente
        if (mejorUltima != null && (System.currentTimeMillis() - mejorUltima.time) < 60_000) {
            onUbicacionObtenida(Ubicacion(mejorUltima.latitude, mejorUltima.longitude), null)
            return
        }

        // Si no, se pide una ubicación nueva. Se responde una sola vez: con la ubicación nueva,
        // o al cumplirse el tiempo máximo (así la pantalla nunca queda esperando para siempre)
        val handler = Handler(Looper.getMainLooper())
        var respondido = false
        lateinit var listener: LocationListener

        val responder = { ubicacion: Ubicacion?, error: String? ->
            if (!respondido) {
                respondido = true
                handler.removeCallbacksAndMessages(null)
                locationManager.removeUpdates(listener)
                onUbicacionObtenida(ubicacion, error)
            }
        }

        // Respaldo: la última ubicación conocida solo si es reciente (una antigua no prueba
        // que el técnico esté en la instalación ahora)
        val responderConRespaldo = { mensajeError: String ->
            val respaldo = mejorUltima?.takeIf { System.currentTimeMillis() - it.time < ANTIGUEDAD_MAXIMA_RESPALDO_MS }
            if (respaldo != null) {
                responder(Ubicacion(respaldo.latitude, respaldo.longitude), null)
            } else {
                responder(null, mensajeError)
            }
        }

        listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                responder(Ubicacion(location.latitude, location.longitude), null)
            }
            @Deprecated("Deprecated in API 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val provider = if (gpsHabilitado) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
        try {
            locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            handler.postDelayed(
                { responderConRespaldo("No se pudo obtener la ubicación. Intente de nuevo, idealmente al aire libre.") },
                TIEMPO_MAXIMO_MS,
            )
        } catch (e: SecurityException) {
            responder(null, "Permisos de ubicación no concedidos")
        } catch (e: Exception) {
            responderConRespaldo("No se pudo obtener la ubicación GPS")
        }
    }
}
