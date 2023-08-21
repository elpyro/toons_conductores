package com.castellanoseloy.toonsconductor.activitys.mapTripActivity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.google.android.gms.maps.model.LatLng

class MapTripActivityViewModel : ViewModel() {

    fun isWazeInstalled(context: Context): Boolean {
        val wazePackageName = "com.waze"
        val packageManager = context.packageManager

        try {
            packageManager.getPackageInfo(wazePackageName, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
    }

    fun isGoogleMapsInstalled(context: Context): Boolean {
        val googleMapsPackageName = "com.google.android.apps.maps"
        val packageManager = context.packageManager

        try {
            packageManager.getPackageInfo(googleMapsPackageName, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
    }


    fun irAWaze(context: Context , destinationLatLng: LatLng) {
        if (destinationLatLng != null) {
            // Construir una URI con la dirección o coordenadas del destino
            val uriDestino = Uri.parse("waze://?ll=${destinationLatLng?.latitude},${destinationLatLng?.longitude}&navigate=yes")

            // Crear un Intent para abrir Waze con la ubicación del destino
            val wazeIntent = Intent(Intent.ACTION_VIEW, uriDestino)
            wazeIntent.setPackage("com.waze")
            context.startActivity(wazeIntent)

        } else {
            Utilidades.mostrarToast("Debes seleccionar un destino", context)
        }
    }


    fun irAGoogleMaps(context: Context , destinationLatLng: LatLng) {
        if (destinationLatLng != null) {
            // Construir una URI con la dirección o coordenadas del destino
            val uriDestino = Uri.parse("google.navigation:q=${destinationLatLng?.latitude},${destinationLatLng?.longitude}")

            // Crear un Intent para abrir Google Maps con la ubicación del destino
            val mapsIntent = Intent(Intent.ACTION_VIEW, uriDestino)
            mapsIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapsIntent)

        } else {
            Utilidades.mostrarToast("Debes seleccionar un destino", context)
        }
    }

    private fun obtenerEnlaceGoogleMaps(context: Context , destinationLatLng: LatLng): String {
        if (destinationLatLng != null) {
            // Construir la URL del enlace de navegación en Google Maps
            return "https://www.google.com/maps/dir/?api=1&destination=${destinationLatLng?.latitude},${destinationLatLng?.longitude}"
        } else {
            Utilidades.mostrarToast("Debes seleccionar un destino", context)
            return ""
        }
    }

    private fun compartirEnlaceWhatsApp(context: Context , destinationLatLng: LatLng) {
        val enlaceGoogleMaps = obtenerEnlaceGoogleMaps(context, destinationLatLng)

        if (enlaceGoogleMaps.isNotEmpty()) {
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            sendIntent.putExtra(Intent.EXTRA_TEXT, enlaceGoogleMaps)

            // Fuerza el uso de WhatsApp para compartir el enlace
            sendIntent.setPackage("com.whatsapp")

            try {
                context.startActivity(sendIntent)
            } catch (e: Exception) {
                Utilidades.mostrarToast("WhatsApp no está instalado en este dispositivo", context)
            }
        }
    }

}