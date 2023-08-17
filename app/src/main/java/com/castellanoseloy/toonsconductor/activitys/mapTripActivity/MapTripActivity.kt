package com.castellanoseloy.toonsconductor.activitys.mapTripActivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.calificationClientActivity.CalificationClientActivity
import com.castellanoseloy.toonsconductor.activitys.mapActivity.MapaActivityViewModel
import com.castellanoseloy.toonsconductor.activitys.utils.CarMoveAnim
import com.castellanoseloy.toonsconductor.databinding.ActivityMapTripBinding
import com.castellanoseloy.toonsconductor.modelos.Booking
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.castellanoseloy.toonsconductor.proveedor.BookingAceptedsProvider
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapTripActivity : AppCompatActivity(), OnMapReadyCallback, Listener, DirectionUtil.DirectionCallBack {

    private var start: LatLng?=null
    private var markerDestination: Marker? =null
    private var isCloseToOrigin: Boolean = false
    private var markerOrigin: Marker? =null
    private lateinit var binding: ActivityMapTripBinding
    private var viewModel: MapaActivityViewModel? = null
    private var googleMaps: GoogleMap? =null
    var easyWayLocation: EasyWayLocation? = null
    private var marcaConductor: Marker? = null
    private  var ubicacionLatLong: LatLng? = null
    private var iconoMarca: BitmapDescriptor? = null
    private var geoProveedor= GeoProveedor()
    private var autenticacion= Autenticacion ()
    private var bookingAceptedsProvider =BookingAceptedsProvider()
    private var isCameraMoved = false // Variable para indicar si moveCamera se ha ejecutado


    private var distance = 0.00
    private var  time= 0.00


    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private var booking:Booking? =null

    //dibujar ruta
    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil

    // DISTANCIA
    private var meters = 0.0
    private var km = 0.0
    private var currentLocation = Location("")
    private var previusLocation = Location("")
    private var isStartedTrip = false

    // TEMPORIZADOR
    private var counter = 0
    private var min = 0
    private var handler = Handler(Looper.myLooper()!!)
    private var runnable = Runnable {
        kotlin.run {
            counter++

            if (min == 0) {
                binding.textViewTimer.text = "$counter Seg"
            }
            else {
                binding.textViewTimer.text = "$min Min $counter Seg"
            }

            if (counter == 60) {
                min = min + (counter / 60)
                counter = 0
                binding.textViewTimer.text = "$min Min $counter Seg"
            }

            startTimer()
        }
    }

    private fun startTimer() {
        handler.postDelayed(runnable, 1000) // INICIALIZAR EL CONTADOR
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding= ActivityMapTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //COLOCAR LA BARRA SUPERIOR TRANSAPENTE
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        viewModel = ViewModelProvider(this).get(MapaActivityViewModel::class.java)

        //cargar mapa del fragmento
        val fragmentManager = supportFragmentManager
        val mapaFragment= fragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapaFragment.getMapAsync(this)

        ubicacionPorDefecto()

        permisoLocalizacion.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        listeners()
    }

    private fun getBooking(){
        bookingAceptedsProvider.getBooking().get().addOnSuccessListener {query->

            if(query!=null){
                if(query.size()>0){
                    booking=  query.documents[0].toObject(Booking::class.java)
                    originLatLng= LatLng(booking!!.originLat!!, booking!!.originLng!!)
                    destinationLatLng=LatLng(booking!!.destinationLat!!, booking!!.destinationLng!!)
                    medirDistacia()
                    dibujarRuta(originLatLng!!)
                    addOriginMarker(originLatLng!!)
                    //getClientInfo()
                }
            }

        }
    }


    private fun listeners() {
        binding.btnFinishTrip.setOnClickListener { updateToFinish() }
        binding.btnStartTrip.setOnClickListener { updateToStarted()}
//        binding.btnDisconnect.setOnClickListener { disconnectDriver() }
    }


    var permisoLocalizacion= registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permiso->
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when{
                permiso.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)->{
                    Log.d("Pemisos","Permiso gps concedido")
                    easyWayLocation!!.startLocation()

                }
                permiso.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)->{
                    Log.d("Pemisos","Permiso gps concedido con limitacion")
                    easyWayLocation!!.startLocation()

                }
                else->{
                    Log.d("Pemisos","Permiso gps NO concedidos")
                }

            }
        }
    }


    private fun salvarLocalizacion(){
        if(ubicacionLatLong!=null){
            geoProveedor.guardarLocalizacionTrabajando(autenticacion.obtenerId(),ubicacionLatLong!!)
        }
    }
    private fun ubicacionPorDefecto() {
        val respuestaLocalizacion= LocationRequest.create().apply {
            interval=0
            fastestInterval= 0
            priority= LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement=1f
        }
        easyWayLocation = EasyWayLocation(this, respuestaLocalizacion, false, false, this)

    }

    private fun showButtonFinish() {
        binding.btnFinishTrip.visibility = View.VISIBLE // MOSTRANDO EL BOTON DE DESCONECTARSE
        binding.btnStartTrip.visibility = View.GONE // OCULATNDO EL BOTON DE CONECTARSE
    }


    fun agregarMarca(){

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

    private fun addOriginMarker(position: LatLng) {
        markerOrigin = googleMaps?.addMarker(MarkerOptions().position(position!!).title("Recogida")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person)))
    }

    private fun addDestinationMarker() {
        if (destinationLatLng != null) {
            markerDestination = googleMaps?.addMarker(MarkerOptions().position(destinationLatLng!!).title("Recoger aqui")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin)))
        }
    }

    override fun onMapReady(mapa: GoogleMap) {
        googleMaps=mapa

        googleMaps?.isMyLocationEnabled=false
        googleMaps?.uiSettings?.isZoomControlsEnabled=true

        // Habilitar el botón de "My Location" (ubicación actual)
        googleMaps?.uiSettings?.isMyLocationButtonEnabled = true

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
    }

    override fun locationOn() {

    }

    //ACTUALIZACION DE LA UBICACION EN TIEMPO REAL

    override fun currentLocation(location: Location) {
        if (ubicacionLatLong!=null) {
            start=ubicacionLatLong
        }
            ubicacionLatLong = LatLng(location.latitude, location.longitude) //POSICION ACTUAL

        //medir distancia
        currentLocation = location
        if (isStartedTrip) {
            meters = meters + previusLocation.distanceTo(currentLocation)
            km = meters / 1000
            binding.textViewDistance.text = "${String.format("%.1f", km)} km"
        }
        previusLocation = location


        if (!isCameraMoved) {

                googleMaps?.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder().target(ubicacionLatLong!!).zoom(17f).build()
                    )
                )
                isCameraMoved = true // Marca moveCamera como ya ejecutado

                crearIcono()
                agregarMarca()
                getBooking()
            }

            if (ubicacionLatLong != null && start != null && marcaConductor!=null) {
                CarMoveAnim.carAnim(marcaConductor!!, start!!, ubicacionLatLong!!)
            }
        medirDistacia()
        salvarLocalizacion()
    }

    private fun medirDistacia() {
        if (booking != null && originLatLng != null) {
            var distance = getDistanceBetween(ubicacionLatLong!!, originLatLng!!)
            if (distance <= 300) {
                isCloseToOrigin = true
            }
            Log.d("LOCATION", "Distance: ${distance}")
        }
    }

    private fun updateToStarted() {
        if (isCloseToOrigin) {
            bookingAceptedsProvider.updateStatus(booking?.idClient!!, "started").addOnCompleteListener {
                if (it.isSuccessful) {
                    if (destinationLatLng != null) {
                        isStartedTrip = true
                        googleMaps?.clear()
                        agregarMarca()
//                        addDirectionMarker(myLocationLatLng!!, angle)
                        dibujarRuta(destinationLatLng!!)
                        markerOrigin?.remove()
                        addDestinationMarker()
                        startTimer()
//                        sendNotification("Viaje iniciado")
                    }
                    showButtonFinish()
                }
            }
        }
        else {
            Toast.makeText(this, "Debes estar mas cerca a la posicion de recogida", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateToFinish() {

        handler.removeCallbacks(runnable) // DETENER CONTADOR
        isStartedTrip = false
        easyWayLocation?.endUpdates()
        geoProveedor.eliminarLocalizacionTrabajo(autenticacion.obtenerId())
        if (min == 0) {
            min = 1
        }
      //  getPrices(km, min.toDouble())
        bookingAceptedsProvider.updateStatus(booking?.idClient!!, "finished").addOnCompleteListener {
            if (it.isSuccessful) {
//                sendNotification("Viaje terminado")
                goToCalificationClient()
            }
        }
    }

    private fun goToCalificationClient() {
        val i = Intent(this, CalificationClientActivity::class.java)
        i.putExtra("price", booking!!.price)
        i.putExtra("origin", booking!!.origin)
        i.putExtra("destination", booking!!.destination)
        i.putExtra("time", time/60)
        i.putExtra("km", km)

        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)

        bookingAceptedsProvider.deleteBookingsForDriver(autenticacion.obtenerId())
    }
    private fun crearIcono() {
        // Crear el BitmapDescriptor una sola vez
        val imagenDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_carro_48)
        iconoMarca = Utilidades.drawableToBitmapDescriptor( imagenDrawable!!)
    }


    override fun locationCancelled() {

    }

    override fun onDestroy() {
        super.onDestroy()
        if(easyWayLocation!=null)  easyWayLocation!!.endUpdates()
    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        //medir distancia y tiempo
        distance = polyLineDetailsArray[1].distance.toDouble() // METROS
        time = polyLineDetailsArray[1].time.toDouble() // SEGUNDOS
        distance = if (distance < 1000.0) 1000.0 else distance // SI ES MENOS DE 1000 METROS EN 1 KM
        time = if (time < 60.0) 60.0 else time

        distance = distance / 1000 // KM
        time = time / 60 // MIN

        val timeString = String.format("%.2f", time)
        val distanceString = String.format("%.2f", distance)

//        getPrices(distance, time)
      //  binding.textViewTimeAndDistance.text = "$timeString mins - $distanceString km"

        //dibujar ruta
        directionUtil.drawPath(WAY_POINT_TAG)
    }

    private fun dibujarRuta(position:LatLng){

        wayPoints.clear()

        wayPoints.add(ubicacionLatLong!!)
        wayPoints.add(position!!)

        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(ubicacionLatLong!!)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMaps!!)
            .setPolyLinePrimaryColor(R.color.black)
            .setPolyLineWidth(25)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(position!!)
            .build()

        directionUtil.initPath()
    }

    //obtener la distacina entre dos puntos
    private fun getDistanceBetween(originLatLng: LatLng, destinationLatLng: LatLng): Float {
        var distance = 0.0f
        val originLocation = Location("")
        val destinationLocation = Location("")

        originLocation.latitude = originLatLng.latitude
        originLocation.longitude = originLatLng.longitude

        destinationLocation.latitude = destinationLatLng.latitude
        destinationLocation.longitude = destinationLatLng.longitude

        distance = originLocation.distanceTo(destinationLocation)
        return distance
    }

}