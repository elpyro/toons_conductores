package com.castellanoseloy.toonsconductor.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.castellanoseloy.toonsconductor.modelos.CarModel
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo

class DatosPersitidos : Service() {

    // Datos que deben persistir
    companion object {
        var datosConductor: ConductorModelo? =null
        var datosCarroEnUso: CarModel? =null
    }

    override fun onBind(intent: Intent?): IBinder? {
        // No necesitamos un enlace para este servicio
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // Inicializar el servicio
        Log.d("Datos persistentes", "Servicio creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Lógica principal del servicio
        Log.d("Datos persistentes", "Servicio iniciado")

        // Acceder o modificar datosPersistidos aquí
        Log.d("Datos persistentes", "Datos conductor: $datosConductor")
        Log.d("Datos persistentes", "Datos vehiculo: $datosCarroEnUso")

        // Devuelve START_STICKY para que el servicio se reinicie si se cierra inesperadamente
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Realizar tareas de limpieza o liberación de recursos
        Log.d("MiServicio", "Servicio destruido")
    }
}
