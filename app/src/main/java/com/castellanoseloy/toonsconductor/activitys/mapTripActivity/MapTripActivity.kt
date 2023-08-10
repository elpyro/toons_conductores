package com.castellanoseloy.toonsconductor.activitys.mapTripActivity

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.mapActivity.MapaActivityViewModel
import com.castellanoseloy.toonsconductor.databinding.ActivityMapTripBinding
import com.castellanoseloy.toonsconductor.fragments.ModalBottomSheetBooking
import com.castellanoseloy.toonsconductor.modelos.Booking
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject

class MapTripActivity : AppCompatActivity(), OnMapReadyCallback, Listener, DirectionUtil.DirectionCallBack {

    private var markerOrigin: Marker? =null
    var bookingListener: ListenerRegistration? =null
    private lateinit var binding: ActivityMapTripBinding
    private var viewModel: MapaActivityViewModel? = null
    private var googleMaps: GoogleMap? =null
    var easyWayLocation: EasyWayLocation? = null
    private var marcaConductor: Marker? = null
    private  var ubicacionLatLong: LatLng? = null
    private var iconoMarca: BitmapDescriptor? = null
    private var geoProveedor= GeoProveedor()
    private var autenticacion= Autenticacion ()
    private var bookingProvider= BookingProvider()
    private var isCameraMoved = false // Variable para indicar si moveCamera se ha ejecutado

    //GOOGLE PLACES
    private var places: PlacesClient? =null
    private var autocompleteOrigen: AutocompleteSupportFragment? =null
    private var autocompleteDestino: AutocompleteSupportFragment? =null
    private var nombreOrigen=""
    private var nombreDestino=""
    private var origenLatLng: LatLng? =null
    private var destinoLatLng: LatLng? = null

    private var distance = 0.00
    private var  time= 0.00

    private var extraOriginName = ""
    private var extraDestinationName = ""
    private var extraOriginLat = 0.0
    private var extraOriginLng = 0.0
    private var extraDestinationLat = 0.0
    private var extraDestinationLng = 0.0
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private var booking:Booking? =null

    //dibujar ruta
    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil

    val modalBooking = ModalBottomSheetBooking()

    val timer = object: CountDownTimer(30000, 1000) {
        override fun onTick(counter: Long) {
            Log.d("TIMER", "Counter: $counter")
        }

        override fun onFinish() {
            Log.d("TIMER", "ON FINISH")
            modalBooking.dismiss()
        }

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
        bookingProvider.getBooking().get().addOnSuccessListener {query->

            if(query!=null){
                if(query.size()>0){
                    booking=  query.documents[0].toObject(Booking::class.java)
                    val originLatLng= LatLng(booking!!.originLat!!, booking!!.originLng!!)
                    dibujarRuta(originLatLng)
                    addOriginMarker(originLatLng)

                    bookingProvider.updateStatus(booking!!.idClient!!,"process").addOnCompleteListener {
                        Utilidades.mostrarToast("Viaje Tomado", this@MapTripActivity)
                    }
                }
            }

        }
    }

    private fun showModalBooking(booking: Booking) {

        val bundle = Bundle()
        bundle.putString("booking", booking.toJson())
        modalBooking.arguments = bundle
        modalBooking.isCancelable = false // NO PUEDA OCULTAR EL MODAL BOTTTOM SHEET

        modalBooking.show(supportFragmentManager, ModalBottomSheetBooking.TAG)

        timer.start()
    }

    private fun listeners() {
//        binding.btnConnect.setOnClickListener { connectDriver() }
//        binding.btnDisconnect.setOnClickListener { disconnectDriver() }
    }

    private val ubicacionPorDefecto = LatLng(4.7109, -74.0721) // Ubicación predeterminada (latitud y longitud)
    private fun centrarCamaraEnUbicacionPorDefecto() {
        googleMaps?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionPorDefecto, 1f))
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

    fun disconnectDriver() {
        easyWayLocation?.endUpdates()
        if (ubicacionLatLong != null) {
            easyWayLocation?.endUpdates()
            centrarCamaraEnUbicacionPorDefecto()
            geoProveedor.eliminarLocalizacion(autenticacion.obtenerId())
        }
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

//    private fun addDestinationMarker() {
//        markerDestination = googleMaps?.addMarker(
//            MarkerOptions().position(destinationLatLng!!).title("LLegada")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin)))
//    }
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

            ubicacionLatLong = LatLng(location.latitude, location.longitude) //POSICION ACTUAL
            if (!isCameraMoved) {

                googleMaps?.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder().target(ubicacionLatLong!!).zoom(17f).build()
                    )
                )
                isCameraMoved = true // Marca moveCamera como ya ejecutado

                crearIcono()
                getBooking()
            }

            agregarMarca()
            salvarLocalizacion()

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

}