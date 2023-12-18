package com.castellanoseloy.toonsconductor.activitys.utils

import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DistanciaAreopuerto {

    // Coordenadas del aeropuerto de Bogotá (El Dorado)
    val aeropuertoLat = 4.697667
    val aeropuertoLng = -74.142290
    // Distancia máxima permitida del aeropuerto
    val maxDistanceKm = 2.8 //0.8 predeterminado

    fun adentroArepouerto(location: Location): Boolean {
        val ubicacionLatLong = convertLocationToLatLng(location)

        // Calcular la distancia entre la ubicación actual y el aeropuerto
        val distanciaAlAeropuerto = calculateDistance(
            ubicacionLatLong?.latitude ?: 0.0,
            ubicacionLatLong?.longitude ?: 0.0,
            aeropuertoLat,
            aeropuertoLng
        )

        // Verificar si la distancia es mayor que la distancia máxima permitida
        if (distanciaAlAeropuerto > maxDistanceKm) {
            Log.d("Localizacion", "Distancia actual del aeropuerto $distanciaAlAeropuerto")
            return false
        }
        return true
    }

    private fun calculateDistance(
        startLat: Double, startLng: Double,
        endLat: Double, endLng: Double
    ): Double {
        val earthRadius = 6371 // Radio de la Tierra en kilómetros

        val dLat = Math.toRadians(endLat - startLat)
        val dLng = Math.toRadians(endLng - startLng)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(startLat)) * cos(Math.toRadians(endLat)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    private fun convertLocationToLatLng(location: Location): LatLng {
        return LatLng(location.latitude, location.longitude)
    }
}
