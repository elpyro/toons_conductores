package com.castellanoseloy.toonsconductor.activitys.mapActivity

import android.Manifest
import android.provider.Settings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.profileActivity.ProfileActivity
import com.castellanoseloy.toonsconductor.activitys.utils.CarMoveAnim
import com.castellanoseloy.toonsconductor.activitys.utils.DistanciaAreopuerto
import com.castellanoseloy.toonsconductor.databinding.ActivityMapBinding
import com.castellanoseloy.toonsconductor.fragments.ModalBottomSheetBooking
import com.castellanoseloy.toonsconductor.fragments.ModalBottomSheetMenu
import com.castellanoseloy.toonsconductor.modelos.Booking
import com.castellanoseloy.toonsconductor.objetos.ErroresFirebase
import kotlin.math.*
import com.castellanoseloy.toonsconductor.proveedor.BookingProvider
import com.castellanoseloy.toonsconductor.proveedor.GeoProveedor
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.optic.uberclonedriverkotlin.providers.DriverProvider
import java.util.Date
import android.content.res.Resources
import android.net.Uri
import android.os.PowerManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.castellanoseloy.toonsconductor.activitys.carList.CarList
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.castellanoseloy.toonsconductor.proveedor.CarProvider
import com.castellanoseloy.toonsconductor.services.ServiceLocalizationManager
import com.google.android.gms.maps.model.MapStyleOptions


class MapaActivity : AppCompatActivity(), OnMapReadyCallback, Listener,DirectionUtil.DirectionCallBack {

    companion object{
        var tiempoActivo: Long? =null

    }

    private val serviceManager = ServiceLocalizationManager.getInstance()
    var viajeAceptado: Boolean =false
    var distanceString: String? =null
    var timeString: String? = null
    var modalMostrado: Boolean=false
    private var bookingQuery: Query? =null
    private var bookingListener: ValueEventListener? =null
    private var booking: Booking? =null

    private var idConductor: String? = null
    private var start: LatLng? =null

    private lateinit var binding: ActivityMapBinding
    var viewModel: MapaActivityViewModel? = null
    private var googleMaps:GoogleMap? =null
    var easyWayLocation: EasyWayLocation? = null
    private var marcaConductor: Marker? = null
    private  var ubicacionLatLong:LatLng? = null
    private var iconoMarca: BitmapDescriptor? = null
    private var geoProveedor= GeoProveedor()
    private var distanciaAreopuerto=DistanciaAreopuerto()
    private var bookingProvider=BookingProvider()
    private val modalMenu = ModalBottomSheetMenu()
    var currentTimerValue: Long ? =null
    private var isCameraMoved = false // Variable para indicar si moveCamera se ha ejecutado

    //dibujar ruta
    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil

    val aeropuertoBogotaLatLng = LatLng(4.701594, -74.146948)

    private val CODIGO_SOLICITUD_PERMISOS = 123

    var modalBooking = ModalBottomSheetBooking()

    // Declara una variable miembro para el PowerManager.WakeLock
    private var wakeLock: PowerManager.WakeLock? = null

    val timer = object: CountDownTimer(10000, 1000) {


        override fun onTick(counter: Long) {
            if (currentTimerValue != counter) {
                Log.d("TIMER", "Timer value changed: $counter")
                currentTimerValue = counter
                viewModel!!.updateCurrentTimerValue(currentTimerValue!!)
            }
        }

        override fun onFinish() {
            Log.d("TIMER", "ON FINISH")
            val idClient= booking?.idClient!!
            bookingProvider.getBooking().get().addOnCompleteListener { task->
                if(task.isSuccessful){
                    bookingProvider.updateStatus(idClient, "cancel").addOnCompleteListener { task->
                        if (task.isSuccessful) {
                            borrarRuta()
                            modalBooking.dismiss()
                            modalMostrado=false
                            listenerBooking()
                            Log.d("FIRESTORE", "booking actualizado a cancelado")
                        }
                    }
                }
            }
            try{
                modalMostrado=false
                borrarRuta()
                modalBooking.dismiss()

            }catch (e: Exception){
                Log.d("FIRESTORE", "booking actualizado a cancelado")
            }
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding= ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Obtiene una instancia del servicio PowerManager
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "MiApp::WakelockTag"
        )

        viewModel = ViewModelProvider(this).get(MapaActivityViewModel::class.java)
        idConductor=DriverProvider.datosConductor?.id!!

        //cargar mapa del fragmento
        val fragmentManager = supportFragmentManager
        val mapaFragment= fragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment
        mapaFragment.getMapAsync(this)

        ubicacionPorDefecto()
        verificarPermisos()

        //si el conductor es nuevo enviar a pantalla de perfil
        if(DriverProvider.datosConductor?.validacion.equals("Valida tu usuario")) goToProfile()

        tiempoActivo= Date().time



        if(DriverProvider.datosConductor!=null){
            listeners()
        }else{
            val driverProvider=DriverProvider()
            val autenticacion= Autenticacion()
            driverProvider.createToken(autenticacion.obtenerId())
            driverProvider.getDriver(autenticacion.obtenerId()).addOnSuccessListener { document ->
                Log.d("Firestore", "Validando client ")
                if (document.exists()) {

                    val driver = document.toObject(ConductorModelo::class.java)
                    DriverProvider.datosConductor=driver

                    listeners()
                }else{
                    Utilidades.mostrarToast("Problemas en conexion", this)
                }
            }
        }



         listeners()
    }


    var permisoLocalizacion= registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permiso->

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when{
                    permiso.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false)->{
                        easyWayLocation!!.startLocation()
                        connectDriver()
                    }
                    else->{
                        Log.d("Pemisos","Permiso gps NO concedidos")
                        mostrarDialogoPermisos()
                    }
            }
            }
    }






    // Función para calcular la distancia entre dos puntos geográficos
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

    private fun goToProfile() {
        val i = Intent(this, ProfileActivity::class.java)
        startActivity(i)
    }

    private fun showModalBooking(booking: Booking) {

        Log.d("Firestore", "Modal mostrado= $modalMostrado para  ${booking}  ")



        if (!modalMostrado) {

            val ubicacionCliente= LatLng(booking?.originLat!!, booking?.originLng!!)
            addClientMarker(ubicacionCliente)
            Log.d("Firestore", "Ubicacion Actual: $ubicacionLatLong")
            ubicacionLatLong?.let { dibujarRutaEntreCliente(it, ubicacionCliente) }

            Log.d("Firestore", "Modal agregado para el booking ${booking}")
            try {
            val bundle = Bundle()
            bundle.putString("booking", booking.toJson())
            modalBooking.arguments = bundle
            modalBooking.isCancelable = false // NO PUEDA OCULTAR EL MODAL BOTTTOM SHEET

            modalMostrado = true // Marcar el modal como mostrado

            modalBooking.show(supportFragmentManager, ModalBottomSheetBooking.TAG)
            timer.start()
            } catch (e: Exception) {
                timer.cancel()
            }

        }
    }



    private fun listeners() {

        binding.btnConnect.setOnClickListener { verificarPermisos() }
        binding.btnDisconnect.setOnClickListener { disconnectDriver() }
        binding.imageViewMenu.setOnClickListener{ showModalMenu() }
    }

    private fun verificarPermisos() {
        permisoLocalizacion.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

    }

    fun detenerServico() {
        serviceManager.stopLocationService(this)
    }

    override fun onResume() {
        super.onResume()
        modalMostrado=false
        // Adquiere el WakeLock al entrar en primer plano
        wakeLock?.acquire()
        listenerBooking()
    }
    private fun showModalMenu() {
        // Verifica si el menú modal ya está agregado al FragmentManager
        val existingMenuFragment = supportFragmentManager.findFragmentByTag(ModalBottomSheetMenu.TAG)

        if (existingMenuFragment == null) {
            // El menú modal no está agregado, así que lo agregamos
            modalMenu.show(supportFragmentManager, ModalBottomSheetMenu.TAG)
        }
    }



    private fun mostrarDialogoPermisos() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permisos Requeridos")
        builder.setMessage("Esta aplicación requiere permisos de ubicación TODO EL TIEMPO, ve a: \n\n↓  Información de la aplicación  \n↓  Permisos  \n↓  Ubicación \n-> PERMITIR TODO EL TIEMPO")
        builder.setPositiveButton("Aceptar") { _, _ ->

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }else{
                solicitarPermisos() // Llama a la función para solicitar permisos
            }

        }
        builder.setCancelable(false) // Evita que el usuario pueda cancelar el diálogo tocando fuera de él
        builder.show()
    }


     private fun solicitarPermisos() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
             ActivityCompat.requestPermissions(
                 this,
                 arrayOf(
                     Manifest.permission.ACCESS_FINE_LOCATION,
                     Manifest.permission.ACCESS_COARSE_LOCATION,
                     Manifest.permission.ACCESS_BACKGROUND_LOCATION
                 ),
                 CODIGO_SOLICITUD_PERMISOS
             )
         }
     }



    private fun ubicacionPorDefecto() {
        val respuestaLocalizacion= LocationRequest.create().apply {
            interval=0
            fastestInterval= 0
            priority=LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement=1f
        }
        easyWayLocation = EasyWayLocation(this, respuestaLocalizacion, false, false, this)
    }

     fun disconnectDriver() {
         detenerServico()
         marcaConductor?.remove()
         easyWayLocation?.endUpdates()
         showButtonConnect()
         easyWayLocation?.endUpdates()
         if(idConductor!=null) geoProveedor.eliminarLocalizacion(idConductor!!)

    }


    private fun connectDriver() {

        val conductorValidado=verificarConductor()
        if(conductorValidado){
            showButtonDisconnect()
            easyWayLocation?.endUpdates() // OTROS HILOS DE EJECUCION
            easyWayLocation?.startLocation()
            isCameraMoved=false
            serviceManager.startLocationService(this)//iniciamos el servicio de localizacion en segundo plano
        }else{
            disconnectDriver()
        }

    }

    private fun verificarConductor() : Boolean{
        if(CarProvider.datosCarroEnUso==null){
            disconnectDriver()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Seleccionar Vehiculo")
            builder.setMessage("Necesita seleccionar un vehiculo de trabajo")
            builder.setPositiveButton("Aceptar") { _, _ ->
                val i = Intent(this, CarList::class.java)
                startActivity(i)
            }
            builder.setCancelable(true) // Evita que el usuario pueda cancelar el diálogo tocando fuera de él
            builder.show()
            return false
        }
        if(!DriverProvider.datosConductor?.validacion.equals("Validado")){
            disconnectDriver()

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Usuario no validado")
            builder.setMessage("El usuario aun no ha sido validado")
            builder.setPositiveButton("Aceptar") { _, _ ->

            }
            builder.setCancelable(true) // Evita que el usuario pueda cancelar el diálogo tocando fuera de él
            builder.show()
            return true //TODO CAMBIAR ESTE VALOR A FALSE, (TRUE SOLO PARA PRUEBAS)
        }
        return true
    }

    private fun showButtonConnect() {
        binding.btnDisconnect.visibility = View.GONE // OCULTANDO EL BOTON DE DESCONECTARSE
        binding.btnConnect.visibility = View.VISIBLE // MOSTRANDO EL BOTON DE CONECTARSE
    }

    private fun showButtonDisconnect() {
        binding.btnDisconnect.visibility = View.VISIBLE // MOSTRANDO EL BOTON DE DESCONECTARSE
        binding.btnConnect.visibility = View.GONE // OCULATNDO EL BOTON DE CONECTARSE
    }

    fun borrarRuta(){
        Log.d("FIRESTORE", "Ruta borrada")
        googleMaps?.clear()
        agregarMarca()
        marcarAreaTrabajo()
    }


    fun agregarMarca(){
        crearIcono()
        if (marcaConductor!=null){
            marcaConductor!!.remove() //no redibujar icono
        }

        if(ubicacionLatLong!=null){
            marcaConductor= googleMaps?.addMarker(
                MarkerOptions()
                    .position(ubicacionLatLong!!)
                    .anchor(0.5f,0.5f)
                    .flat(true)
                    .icon(iconoMarca)
            )
        }
    }

    private fun addClientMarker(clientLatLng:LatLng) {
     googleMaps?.addMarker(MarkerOptions().position(clientLatLng!!).title("Mi posicion")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location_red)))
    }

    override fun onMapReady(mapa: GoogleMap) {

        googleMaps=mapa

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            val success = googleMaps?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.styles)
            )
            if (!success!!) {
                Log.d("MAPAS", "No se pudo encontrar el estilo")
            }

        } catch (e: Resources.NotFoundException) {
            Log.d("MAPAS", "Error: ${e.toString()}")
        }

        googleMaps?.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(aeropuertoBogotaLatLng!!).zoom(15f).build()
            )
        )


        googleMaps?.isMyLocationEnabled=true
        googleMaps?.uiSettings?.isZoomControlsEnabled=true
        // Habilitar el botón de "My Location" (ubicación actual)
        googleMaps?.uiSettings?.isMyLocationButtonEnabled = true

        marcarAreaTrabajo()



    }

    private fun marcarAreaTrabajo() {
        val circleOptions = CircleOptions()
            .center(LatLng(distanciaAreopuerto.aeropuertoLat, distanciaAreopuerto.aeropuertoLng)) // Ubicación del centro del círculo (aeropuerto)
            .radius(distanciaAreopuerto.maxDistanceKm*1000) // Radio en metros
            .strokeWidth(2f) // Ancho de la línea del borde del círculo
            .strokeColor(Color.GREEN) // Color del borde del círculo
            .fillColor(Color.argb(25, 0, 255, 0)) // Color del relleno del círculo (con transparencia)

        // Agregar el círculo al mapa
        googleMaps?.addCircle(circleOptions)
    }

    override fun locationOn() {

    }


    override fun currentLocation(location: Location) {

            if (ubicacionLatLong!=null) {
                 start=ubicacionLatLong
            }
            ubicacionLatLong = LatLng(location.latitude, location.longitude) //POSICION ACTUAL

            if (!isCameraMoved) {
                googleMaps?.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder().target(ubicacionLatLong!!).zoom(17f).build()
                    )
                )
                isCameraMoved = true // Marca moveCamera como ya ejecutado

                if(binding?.btnDisconnect?.isVisible == true) agregarMarca()
            }

            if (ubicacionLatLong != null && start != null && marcaConductor!=null) {
                CarMoveAnim.carAnim(marcaConductor!!, start!!, ubicacionLatLong!!)
            }
           // salvarLocalizacion()

        // Calcular la distancia entre la ubicación actual y el aeropuerto
        val distanciaAlAeropuerto = calculateDistance(
            ubicacionLatLong?.latitude ?: 0.0,
            ubicacionLatLong?.longitude ?: 0.0,
            distanciaAreopuerto.aeropuertoLat,
            distanciaAreopuerto. aeropuertoLng
        )

        Log.d("Firestore", "Distancia del arepuerto $distanciaAlAeropuerto")
        // Verificar si la distancia es mayor que la distancia máxima permitida
        val distanciaAreopuerto=distanciaAreopuerto.adentroArepouerto(location)
        if (!distanciaAreopuerto) {
            // Si la distancia es mayor, desconectar al conductor
            val rootView = findViewById<View>(android.R.id.content) // Puedes obtener la vista raíz de la actividad o el diseño
            Snackbar.make(rootView, "Estas fuera del area de trabajo", Snackbar.LENGTH_LONG).show()
            disconnectDriver()
        }
    }

    private fun crearIcono() {
        // Crear el BitmapDescriptor una sola vez
        val imagenDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_carro_48)
        iconoMarca = Utilidades.drawableToBitmapDescriptor( imagenDrawable!!)
    }


    override fun locationCancelled() {

    }

    
    //mostrar cliente
     fun listenerBooking() {

         bookingQuery= bookingProvider.getBooking()


        bookingListener = bookingQuery!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Firestore", "Esperando bookin")
                // Este método se llama cuando los datos que cumplen con la consulta cambian

                // Aquí puedes procesar los datos actualizados
                for (childSnapshot in snapshot.children) {
                     booking = childSnapshot.getValue(Booking::class.java)
                    if (booking?.status == "create") {
                        Log.d("REALTIME DATABASE", "Booking cargado: $booking")
                        showModalBooking(booking!!)
                        break // Sal del bucle si se encuentra un booking válido
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Este método se llama si ocurre un error al recuperar los datos
                Log.d("REALTIME DATABASE", "ERROR: ${error.message}")
                ErroresFirebase.imprimirError(error.toException(), this@MapaActivity)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        // Libera el WakeLock al salir del primer plano
        wakeLock?.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        geoProveedor.eliminarLocalizacion(idConductor!!)

        if(easyWayLocation!=null)  easyWayLocation!!.endUpdates()
        Log.d("FIRESTORE", "Booking listenener detenido")

        bookingQuery?.removeEventListener(bookingListener!!)
    }


    private fun dibujarRutaEntreCliente(originLatLng:LatLng,destinationLatLng:LatLng){
        wayPoints.clear()
        wayPoints.add(originLatLng!!)
        wayPoints.add(destinationLatLng!!)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(originLatLng!!)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMaps!!)
            .setPolyLinePrimaryColor(R.color.black)
            .setPolyLineWidth(25)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(destinationLatLng!!)
            .build()

        directionUtil.initPath()
    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        //medir distancia y tiempo
        var distance = polyLineDetailsArray[1].distance.toDouble() // METROS
        var time = polyLineDetailsArray[1].time.toDouble() // SEGUNDOS
        distance = if (distance < 1000.0) 1000.0 else distance // SI ES MENOS DE 1000 METROS EN 1 KM
        time = if (time < 60.0) 60.0 else time

        distance = distance / 1000 // KM
        time = time / 60 // MIN

        timeString = String.format("%.2f", time)
        distanceString = String.format("%.2f", distance)
        Log.d("Firestore", "Distancia entre Cliente: $distanceString tiempo: $timeString")


        //dibujar ruta
        directionUtil.drawPath(WAY_POINT_TAG)
    }
}