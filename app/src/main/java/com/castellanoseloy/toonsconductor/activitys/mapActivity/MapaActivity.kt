package com.castellanoseloy.toonsconductor.activitys.mapActivity

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.utils.CarMoveAnim
import com.castellanoseloy.toonsconductor.databinding.ActivityMapBinding
import com.castellanoseloy.toonsconductor.fragments.ModalBottomSheetBooking
import com.castellanoseloy.toonsconductor.modelos.Booking
import com.castellanoseloy.toonsconductor.objetos.ErroresFirebase
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.castellanoseloy.toonsconductor.proveedor.BookingProvider
import com.castellanoseloy.toonsconductor.proveedor.GeoProveedor
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.firebase.firestore.ListenerRegistration


class MapaActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    private var start: LatLng? =null
    var bookingListener: ListenerRegistration? =null
    private lateinit var binding: ActivityMapBinding
    private var viewModel: MapaActivityViewModel? = null
    private var googleMaps:GoogleMap? =null
    var easyWayLocation: EasyWayLocation? = null
    private var marcaConductor: Marker? = null
    private  var ubicacionLatLong:LatLng? = null
    private var iconoMarca: BitmapDescriptor? = null
    private var geoProveedor= GeoProveedor()
    private var autenticacion=Autenticacion ()
    private var bookingProvider=BookingProvider()


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

        binding= ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //COLOCAR LA BARRA SUPERIOR TRANSAPENTE
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        viewModel = ViewModelProvider(this).get(MapaActivityViewModel::class.java)

        //cargar mapa del fragmento
        val fragmentManager = supportFragmentManager
        val mapaFragment= fragmentManager.findFragmentById(R.id.mapa) as SupportMapFragment
        mapaFragment.getMapAsync(this)

        ubicacionPorDefecto()

        permisoLocalizacion.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

         listeners()
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
        listenerBooking()
        binding.btnConnect.setOnClickListener { connectDriver() }
        binding.btnDisconnect.setOnClickListener { disconnectDriver() }
    }

    private val ubicacionPorDefecto = LatLng(4.7109, -74.0721) // Ubicación predeterminada (latitud y longitud)
    private fun centrarCamaraEnUbicacionPorDefecto() {
        googleMaps?.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionPorDefecto, 1f))
    }
    var permisoLocalizacion= registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permiso->
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when{
                permiso.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)->{
                    Log.d("Pemisos","Permiso gps concedido")
                    easyWayLocation!!.startLocation()
                    verificarConductorConectado()
                }
                permiso.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)->{
                    Log.d("Pemisos","Permiso gps concedido con limitacion")
                    easyWayLocation!!.startLocation()
                    verificarConductorConectado()
                }
                else->{
                    Log.d("Pemisos","Permiso gps NO concedidos")
                }

            }
        }
    }


    private fun salvarLocalizacion(){
        if(ubicacionLatLong!=null){
            geoProveedor.guardarLocalizacion(autenticacion.obtenerId(),ubicacionLatLong!!)
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
        easyWayLocation?.endUpdates()
        if (ubicacionLatLong != null) {
            showButtonConnect()
            easyWayLocation?.endUpdates()
            marcaConductor?.remove()
            geoProveedor.eliminarLocalizacion(autenticacion.obtenerId())
        }
    }
    private fun connectDriver() {
        easyWayLocation?.endUpdates() // OTROS HILOS DE EJECUCION
        easyWayLocation?.startLocation()
        showButtonDisconnect()
        isCameraMoved=false
    }

    private fun showButtonConnect() {
        binding.btnDisconnect.visibility = View.GONE // OCULTANDO EL BOTON DE DESCONECTARSE
        binding.btnConnect.visibility = View.VISIBLE // MOSTRANDO EL BOTON DE CONECTARSE
    }

    private fun showButtonDisconnect() {
        binding.btnDisconnect.visibility = View.VISIBLE // MOSTRANDO EL BOTON DE DESCONECTARSE
        binding.btnConnect.visibility = View.GONE // OCULATNDO EL BOTON DE CONECTARSE
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
    private var isCameraMoved = false // Variable para indicar si moveCamera se ha ejecutado
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

                agregarMarca()
            }

            if (ubicacionLatLong != null && start != null && marcaConductor!=null) {
                CarMoveAnim.carAnim(marcaConductor!!, start!!, ubicacionLatLong!!)
            }

//            agregarMarca()
            salvarLocalizacion()
    }

    private fun crearIcono() {
        // Crear el BitmapDescriptor una sola vez
        val imagenDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_carro_48)
        iconoMarca = Utilidades.drawableToBitmapDescriptor( imagenDrawable!!)
    }

    private fun verificarConductorConectado(){
        geoProveedor.obtenerLocalizacion(autenticacion.obtenerId(),this@MapaActivity)
            .addOnSuccessListener { documento->
                if(documento.exists()){
                    if(documento.contains("l")){
                        connectDriver()
                    }else{
                        showButtonDisconnect()
                    }
                }else{
                    showButtonDisconnect()
                }
            }
    }
    override fun locationCancelled() {

    }

    
    //mostrar cliente
    private fun listenerBooking() {
        bookingListener = bookingProvider.getBooking().addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d("FIRESTORE", "ERROR: ${e.message}")
                ErroresFirebase.imprimirError(e,this@MapaActivity)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                if (snapshot.documents.size > 0) {
                    val booking = snapshot.documents[0].toObject(Booking::class.java)
                    if (booking?.status == "create") {
                        showModalBooking(booking!!)

                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
//        if(easyWayLocation!=null) easyWayLocation!!.endUpdates()
//        disconnectDriver()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(easyWayLocation!=null)  easyWayLocation!!.endUpdates()
        disconnectDriver()
        bookingListener?.remove()
    }

}