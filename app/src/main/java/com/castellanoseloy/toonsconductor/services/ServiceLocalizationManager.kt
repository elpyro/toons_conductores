package com.castellanoseloy.toonsconductor.services

import android.content.Intent

import android.content.Context
import android.util.Log

class ServiceLocalizationManager private constructor() {

    companion object {
        private var instance: ServiceLocalizationManager? = null

        @Synchronized
        fun getInstance(): ServiceLocalizationManager {
            if (instance == null) {
                instance = ServiceLocalizationManager()
            }
            return instance!!
        }
    }

    fun startLocationService(context: Context) {
        val serviceIntent = Intent(context, LocationService::class.java)
        context.startService(serviceIntent)
    }

    fun stopLocationService(context: Context) {
        val serviceIntent = Intent(context, LocationService::class.java)
        context.stopService(serviceIntent)

        Log.d("Localizacion", "El servicio de localizacion  fue detenido")
    }

}
