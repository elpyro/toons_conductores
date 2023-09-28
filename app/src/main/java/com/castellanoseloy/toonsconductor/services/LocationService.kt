package com.castellanoseloy.toonsconductor.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.castellanoseloy.toonsconductor.R
import android.os.Handler
import com.castellanoseloy.toonsconductor.activitys.mapActivity.MapaActivity
import com.castellanoseloy.toonsconductor.activitys.utils.DistanciaAreopuerto
import com.castellanoseloy.toonsconductor.proveedor.GeoProveedor
import com.castellanoseloy.toonsconductor.utils.Utilidades.convertLocationToLatLng
import com.optic.uberclonedriverkotlin.providers.DriverProvider

class LocationService : Service() {

    private val binder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val geoProveedor= GeoProveedor()
    private var isServiceRunning = false  //verificacion del que el servicio fue detenido
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = FusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    // Aquí colocas tu código de currentLocation y sus funciones anidadas
                    // Por ejemplo:
                    handleNewLocation(location)
                    // ...
                }
            }
        }
    }
    private val handler = Handler()
    private fun scheduleNotificationUpdate() {
        handler.postDelayed({
            Log.d("Localizacion", "Notificacion actualizada")
            requestLocationUpdates()
            scheduleNotificationUpdate() // Programa la próxima actualización
        }, NOTIFICATION_UPDATE_INTERVAL.toLong()) // Define el intervalo entre actualizaciones en milisegundos
    }
    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(FOREGROUND_SERVICE_ID, notification)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Establece isServiceRunning en true
        isServiceRunning = true
        // Configura la notificación para el servicio en primer plano.
        val notification = createNotification()
//
//        // Inicia el servicio en primer plano con la notificación.
        startForeground(FOREGROUND_SERVICE_ID, notification)

        scheduleNotificationUpdate()

        // Solicita actualizaciones de ubicación.
        requestLocationUpdates()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        removeLocationUpdates()
        handler.removeCallbacksAndMessages(null)

        geoProveedor.eliminarLocalizacion(DriverProvider.datosConductor?.id!!)
        Log.d("Localizacion", "Conductor desconectado por onDestroy")

        // Detén el servicio
        stopSelf()
    }

    private fun createNotification(): Notification {
        val channelId = "YourChannelId"
        val channelName = "YourChannelName"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Ubicación en segundo plano activada")
            .setSmallIcon(R.drawable.my_location_red)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSound(null) // Desactivar sonido de la notificación
            .setVibrate(null) // Desactivar vibración de la notificación

        // Importante: No configures un PendingIntent para la notificación si no deseas que haga nada cuando se toque.

        return notificationBuilder.build()
    }


    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val looper = Looper.myLooper() ?: Looper.getMainLooper() // Obtener el Looper principal si es nulo
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        locationResult.lastLocation?.let { location ->
                            // Aquí puedes manejar la ubicación obtenida
                            handleNewLocation(location)

                            // Detener las actualizaciones de ubicación después de obtener la ubicación
                            removeLocationUpdates()
                        }
                    }
                },
                looper
            )
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d("LocalizacionServicio", "La aplicación se ha cerrado inesperadamente o en segundo plano")

        // Cancela las actualizaciones programadas
        handler.removeCallbacksAndMessages(null)

        geoProveedor.eliminarLocalizacion(DriverProvider.datosConductor?.id!!)
        Log.d("Localizacion", "Conductor desconectado por cierre inesperado")

        // Detén el servicio
        stopSelf()
    }




    private fun removeLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun handleNewLocation(location: Location) {

        if (isServiceRunning) {
            // Coloca aquí tu código de currentLocation y sus funciones anidadas
            // ...
            val distanciaAreopuerto = DistanciaAreopuerto()
            val dentroAreopuerto = distanciaAreopuerto.adentroArepouerto(location)
            Log.d("Localizacion", "El conductor esta dentro del areopuerto: $dentroAreopuerto")
            if (!dentroAreopuerto) {
                geoProveedor.eliminarLocalizacion(DriverProvider.datosConductor?.id!!)
                Log.d("Localizacion", "Conductor desconectado por salir de area de trabajo")

                // Detiene el servicio actual
                handler.removeCallbacksAndMessages(null)
                stopSelf()
            } else {
                val ubicacion = convertLocationToLatLng(location)
                val tiempoActivo = MapaActivity.tiempoActivo


                geoProveedor.guardarLocalizacion(
                    DriverProvider.datosConductor?.id!!,
                    ubicacion!!,
                    tiempoActivo!!
                )
                Log.d("Localizacion", "Ubicacion del conductor  actualizada en segundo plano")


            }
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): LocationService {
            return this@LocationService
        }
    }

    companion object {
        const val FOREGROUND_SERVICE_ID = 12345 // ID de notificación de servicio en primer plano
        const val NOTIFICATION_UPDATE_INTERVAL = 10000 // Intervalo en mil
    }
}