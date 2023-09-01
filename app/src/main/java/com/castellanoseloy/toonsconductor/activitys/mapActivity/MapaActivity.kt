package com.castellanoseloy.toonsconductor.activitys.mapActivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.castellanoseloy.toonsconductor.databinding.ActivityMapBinding
import com.castellanoseloy.toonsconductor.fragments.ModalBottomSheetBooking
import com.castellanoseloy.toonsconductor.fragments.ModalBottomSheetMenu
import com.castellanoseloy.toonsconductor.modelos.Booking
import com.castellanoseloy.toonsconductor.modelos.ConductorModelo
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.optic.uberclonedriverkotlin.providers.DriverProvider
import java.util.Date


class MapaActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    var modalMostrado: Boolean=false
    private var bookingQuery: Query? =null
    private var bookingListener: ValueEventListener? =null
    private var booking: Booking? =null
    private var primeraCarga: Boolean? =false
    private var tiempoActivo: Long? =null
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
    private var autenticacion=Autenticacion ()
    private var bookingProvider=BookingProvider()
    private val modalMenu = ModalBottomSheetMenu()
    var currentTimerValue: Long ? =null
    private var driverProvideer=DriverProvider()

    val modalBooking = ModalBottomSheetBooking()

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
                            modalBooking.dismiss()
                            modalMostrado=false
                            listenerBooking()
                            Log.d("FIRESTORE", "booking actualizado a cancelado")
                        }
                    }
                }
            }

            modalBooking.dismiss()

        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding= ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //COLOCAR LA BARRA SUPERIOR TRANSAPENTE
       // window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

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

        idConductor=autenticacion.obtenerId()


        driverProvideer.getDriver(autenticacion.obtenerId()).addOnSuccessListener { document ->
            Log.d("Firestore", "Validando conductor "+autenticacion.obtenerId())
            if (document.exists()) {
                val driver = document.toObject(ConductorModelo::class.java)
                DriverProvider.datosConductor = driver
                Log.d("Firestore", "Validacion de conductor ${driver?.validacion}")
                if(driver?.validacion.equals("Valida tu usuario")) goToProfile()
            }else{
                Log.d("Firestore", "No se encontraron datos del conductor")
            }
        }

        tiempoActivo= Date().time
         listeners()
    }

    private fun goToProfile() {
        val i = Intent(this, ProfileActivity::class.java)
        startActivity(i)
    }

    private fun showModalBooking(booking: Booking) {


            Log.d("Firestore", "Modal agregado para el booking ${booking}")

// En tu código donde se muestra el modal
        if (!modalMostrado) {
            val bundle = Bundle()
            bundle.putString("booking", booking.toJson())
            modalBooking.arguments = bundle
            modalBooking.isCancelable = false // NO PUEDA OCULTAR EL MODAL BOTTTOM SHEET

            modalBooking.show(supportFragmentManager, ModalBottomSheetBooking.TAG)
            timer.start()
            primeraCarga = false
            modalMostrado = true // Marcar el modal como mostrado
        }

    }

    private fun listeners() {
        listenerBooking()
        binding.btnConnect.setOnClickListener { connectDriver() }
        binding.btnDisconnect.setOnClickListener { disconnectDriver() }
        binding.imageViewMenu.setOnClickListener{ showModalMenu() }
    }

    private fun showModalMenu() {
        // Verifica si el menú modal ya está agregado al FragmentManager
        val existingMenuFragment = supportFragmentManager.findFragmentByTag(ModalBottomSheetMenu.TAG)

        if (existingMenuFragment == null) {
            // El menú modal no está agregado, así que lo agregamos
            modalMenu.show(supportFragmentManager, ModalBottomSheetMenu.TAG)
        }
    }

    private val ubicacionPorDefecto = LatLng(4.7109, -74.0721) // Ubicación predeterminada (latitud y longitud)

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
            geoProveedor.guardarLocalizacion(autenticacion.obtenerId(),ubicacionLatLong!!, tiempoActivo!!)
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
           
           if(idConductor!=null) geoProveedor.eliminarLocalizacion(idConductor!!)
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

        googleMaps?.isMyLocationEnabled=true
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
//        if(easyWayLocation!=null) easyWayLocation!!.endUpdates()
//        disconnectDriver()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(easyWayLocation!=null)  easyWayLocation!!.endUpdates()
        disconnectDriver()
        Log.d("FIRESTORE", "Booking listenener detenido")
        bookingQuery?.removeEventListener(bookingListener!!)
    }

}