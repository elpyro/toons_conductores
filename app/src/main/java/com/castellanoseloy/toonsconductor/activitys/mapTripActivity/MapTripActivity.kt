package com.castellanoseloy.toonsconductor.activitys.mapTripActivity

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.calificationClientActivity.CalificationClientActivity
import com.castellanoseloy.toonsconductor.activitys.utils.CarMoveAnim
import com.castellanoseloy.toonsconductor.activitys.utils.Sonidos
import com.castellanoseloy.toonsconductor.activitys.viajeCancelado.ViajeCancelado
import com.castellanoseloy.toonsconductor.databinding.ActivityMapTripBinding
import com.castellanoseloy.toonsconductor.fragments.ModalBottomTripInfo
import com.castellanoseloy.toonsconductor.modelos.BookingModel
import com.castellanoseloy.toonsconductor.modelos.TransaccionesModelo
import com.castellanoseloy.toonsconductor.objetos.ErroresFirebase
import com.castellanoseloy.toonsconductor.promts.CancelacionMotivo
import com.castellanoseloy.toonsconductor.proveedor.BookingAceptedsProvider
import com.castellanoseloy.toonsconductor.proveedor.ClienteProveedor
import com.castellanoseloy.toonsconductor.proveedor.GeoProveedor
import com.castellanoseloy.toonsconductor.proveedor.TransaccionesProvider
import com.castellanoseloy.toonsconductor.services.ServiceLocalizationManager
import com.castellanoseloy.toonsconductor.utils.NotificarEstados
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.optic.uberclonedriverkotlin.models.Client
import com.optic.uberclonedriverkotlin.models.History
import com.optic.uberclonedriverkotlin.providers.DriverProvider
import com.optic.uberclonedriverkotlin.providers.HistoryProvider
import java.util.Date


class MapTripActivity : AppCompatActivity(), OnMapReadyCallback, Listener, DirectionUtil.DirectionCallBack {

    private var avisoLLegando: Boolean=false
    private lateinit var bookingQuery: Query
    private lateinit var bookingListener: ValueEventListener

    private var idTransaccion: String? = null
    private var client: Client? = null
    private var start: LatLng?=null
    private var markerDestination: Marker? =null
    private var isCloseToOrigin: Boolean = false
    private var markerOrigin: Marker? =null
    private lateinit var binding: ActivityMapTripBinding

    private var viewModel: MapTripActivityViewModel? = null
    private var googleMaps: GoogleMap? =null
    var easyWayLocation: EasyWayLocation? = null
    private var marcaConductor: Marker? = null
    private  var ubicacionLatLong: LatLng? = null
    private var iconoMarca: BitmapDescriptor? = null

    private var geoProveedor= GeoProveedor()
    private lateinit var sonidos: Sonidos
    private var bookingAceptedsProvider =BookingAceptedsProvider()
    private val historyProvider = HistoryProvider()
    private val modalTrip=ModalBottomTripInfo()
    private val clientProvider = ClienteProveedor()
    private val notificarEstados=NotificarEstados()

    val aeropuertoBogotaLatLng = LatLng(4.701594, -74.146948)


    private var isCameraMoved = false // Variable para indicar si moveCamera se ha ejecutado

    private var distance = 0.00
    private var  time= 0.00

    private var originLatLng: LatLng? = null
    private var destinoOtrosMapas: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private var booking:BookingModel? =null

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



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding= ActivityMapTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sonidos = Sonidos(this)

        detenerServico()

        viewModel = ViewModelProvider(this).get(MapTripActivityViewModel::class.java)

        //cargar mapa del fragmento
        val fragmentManager = supportFragmentManager
        val mapaFragment= fragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapaFragment.getMapAsync(this)

        progressDialog.show()

        ubicacionPorDefecto()
        getBooking()

        permisoLocalizacion.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))


        listeners()
    }

    private fun getBooking(){

        bookingQuery = bookingAceptedsProvider.getBookingAccepteds()

        bookingListener = bookingQuery!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (childSnapshot in snapshot.children) {

                    booking = childSnapshot.getValue(BookingModel::class.java)

                    if (booking!!.status.equals("Cancelado")) {
                        viajeCancelado()
                    }else {
                        detallesViaje()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Este método se llama si ocurre un error al recuperar los datos
                Log.d("REALTIME DATABASE", "ERROR: ${error.message}")
                ErroresFirebase.imprimirError(error.toException(), this@MapTripActivity)
            }
        })
    }

    // Método para detener el escuchador cuando ya no sea necesario
    private fun detenerEscuchador() {
        // Verifica si la referencia del escuchador no es nula antes de intentar quitar el escuchador
        bookingListener?.let {
            bookingQuery?.removeEventListener(it)
        }
    }

    private fun viajeCancelado() {
            detenerEscuchador()

            val i = Intent(this, ViajeCancelado::class.java)

            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
    }

    private fun detallesViaje() {

        mostrarBotonesOcultos(booking?.status)

        originLatLng = LatLng(booking!!.originLat!!, booking!!.originLng!!)

        destinationLatLng = LatLng(booking!!.destinationLat!!, booking!!.destinationLng!!)

        idTransaccion=booking!!.id

        if(booking!!.status.equals("Viaje en proceso")){
            destinoOtrosMapas=destinationLatLng
            dibujarRuta(destinationLatLng!!)
            addOriginMarker(destinationLatLng!!)
            mostrarBotonesViajeIniciado()
            binding?.buttonCancelar?.visibility=View.GONE
        } else{
            destinoOtrosMapas=originLatLng
            dibujarRuta(originLatLng!!)
            addOriginMarker(originLatLng!!)
            binding?.buttonCancelar?.visibility=View.VISIBLE
        }

        getClientInfo()

        progressDialog.dismiss()
    }


    private fun mostrarBotonesOcultos(status: String?) {
        binding?.imageViewInfo?.visibility=View.VISIBLE


        mostrarSegunStatus(status)

        var wazeIntalado= viewModel?.isWazeInstalled(this)
        var googleMapsInstado= viewModel?.isGoogleMapsInstalled(this)

        if(!wazeIntalado!!) binding.imageViewWaze.visibility=View.GONE
        if(!googleMapsInstado!!) binding.imageViewGoogleMaps.visibility=View.GONE

        //cuando incia el primer punto de busqueda es en el origen
        binding.imageViewWaze.setOnClickListener{ viewModel!!.irAWaze(this,destinoOtrosMapas!!) }
        binding.imageViewGoogleMaps.setOnClickListener{ viewModel!!.irAGoogleMaps(this,destinoOtrosMapas!!) }
    }

    private fun mostrarSegunStatus(status: String?) {

        if(status.equals("accept")) viewModel?.mostrarBotonIrAPuntoEncuentro(binding,this,destinoOtrosMapas)
        if(status.equals("Al punto de encuentro"))  viewModel?.mostrarBotonHeLlegado(binding,this,destinoOtrosMapas)
        if(status.equals("Ha llegado al punto de encuentro"))  viewModel?.mostrarBottonIniciarViaje(binding,this,destinoOtrosMapas)
        if(status.equals("Viaje en proceso")) {
            viewModel?.showButtonFinish(binding,this,destinoOtrosMapas)

        }

    }


    private fun listeners() {

        binding.btnStartTrip.setOnClickListener { actualizarAlEncuentro()}
        binding.btnHellegado.setOnClickListener { heLLegado()}
        binding.btnInciarViaje.setOnClickListener { inicarViaje()}
        binding.btnFinishTrip.setOnClickListener { updateToFinish() }
        binding.imageViewInfo.setOnClickListener{ showModalInfo() }
        binding.buttonCancelar.setOnClickListener { cancelarViaje() }

    }

    private fun cancelarViaje() {
        val history=createHistory()

        var motivo= CancelacionMotivo()
        motivo.mensaje(this, history!!)

    }

    private fun showModalInfo() {

        val existingMenuFragment = supportFragmentManager.findFragmentByTag(ModalBottomTripInfo.TAG)

        if (existingMenuFragment == null) {
            if(booking!= null){
                var bundle=Bundle()
                bundle.putString("booking" , booking?.toJson())
                modalTrip.arguments=bundle
                modalTrip.show(supportFragmentManager,ModalBottomTripInfo.TAG )
            }else{
                Utilidades.mostrarToast("La información se esta cargando", this)

            }
        }

    }

    private fun createHistory(): History {
        return History(
            id = idTransaccion,
            idDriver = DriverProvider.datosConductor?.id,
            idClient = booking?.idClient,
            pasajero=booking?.nombrePasajero,
            pasajeroTelefono=booking?.telefonoPasajero,
            origin = booking?.origin,
            destination = booking?.destination,
            originLat = booking?.originLat,
            originLng = booking?.originLng,
            destinationLat = booking?.destinationLat,
            destinationLng = booking?.destinationLng,
            tarifas = booking?.tarifas,
            timestamp = Date().time,
            car = booking?.car

        )
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
            geoProveedor.guardarLocalizacionTrabajando(DriverProvider.datosConductor?.id!!,ubicacionLatLong!!)
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
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location_white)))
    }

    private fun addDestinationMarker() {
        if (destinationLatLng != null) {
            markerDestination = googleMaps?.addMarker(MarkerOptions().position(destinationLatLng!!).title("Recoger aqui")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location_white)))
        }
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
                CameraPosition.builder().target(aeropuertoBogotaLatLng!!).zoom(14f).build()
            )
        )

        googleMaps?.isMyLocationEnabled=true
        googleMaps?.uiSettings?.isZoomControlsEnabled=true
        // Habilitar el botón de "My Location" (ubicación actual)
        googleMaps?.uiSettings?.isMyLocationButtonEnabled = true

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

                dibujarRuta(destinoOtrosMapas!!)
                crearIcono()
                agregarMarca()

            }

            if (ubicacionLatLong != null && start != null && marcaConductor!=null) {
                CarMoveAnim.carAnim(marcaConductor!!, start!!, ubicacionLatLong!!)
            }
        medirDistacia()
        salvarLocalizacion()
    }

    private fun medirDistacia() {

        if (ubicacionLatLong != null && destinoOtrosMapas != null) {
            var distance = Utilidades.obtenerDistacia(ubicacionLatLong!!, destinoOtrosMapas!!)
            if (distance < 300) {
                if(booking!!.status.equals("Viaje en proceso")){
                   if(!avisoLLegando){
                       sonidos.crearTonoLLegandoAlDestino()
                       avisoLLegando = true
                   }
                } else{
                    if(distance<30) heLLegado()
                }

            }

            Log.d("Distancia", "Distance: ${distance}")
        }
    }

    private fun actualizarAlEncuentro() {

            bookingAceptedsProvider.updateStatus(booking?.idDriver!!, "Al punto de encuentro").addOnCompleteListener {
                if (it.isSuccessful) {
                    binding.LinearCompartirRuta.visibility= View.VISIBLE
                    viewModel?.preguntarNavegador(this,destinoOtrosMapas)
                    if (destinationLatLng != null) {
                        isStartedTrip = true

                        notificarEstados.sendNotification("El conductor va en camino al punto de encuentro",client!!,this)
                    }
                    viewModel?.mostrarBotonHeLlegado(binding,this,destinoOtrosMapas)
                }else{
                    Toast.makeText(this, "Error de conexion", Toast.LENGTH_LONG).show()
                    Log.d("Booking", "ERROR: ${booking}")
                    getBooking()
                }
            }
    }



    private fun heLLegado() {

        bookingAceptedsProvider.updateStatus(booking?.idDriver!!, "Ha llegado al punto de encuentro").addOnCompleteListener {
            if (it.isSuccessful) {
                binding?.LinearCompartirRuta?.visibility=View.GONE
                binding.btnHellegado.visibility = View.GONE
                if (destinationLatLng != null) {
                    isStartedTrip = true
                    googleMaps?.clear()
                    agregarMarca()

                    markerOrigin?.remove()

                    destinoOtrosMapas=destinationLatLng //establecer la alerta de proximidad

                    sonidos.crearTonoPuntoEncuentro()

                    notificarEstados.sendNotification("El conductor ha llegado a tu encuentro",client!!,this)
                }
                viewModel?.mostrarBottonIniciarViaje(binding,this,destinoOtrosMapas)
            }else{
                Toast.makeText(this, "Error de conexion", Toast.LENGTH_LONG).show()
                Log.d("Booking", "ERROR: ${booking}")
                getBooking()
            }
        }
    }

    private fun inicarViaje() {

        bookingAceptedsProvider.updateStatus(booking?.idDriver!!, "Viaje en proceso").addOnCompleteListener {
            if (it.isSuccessful) {

                mostrarBotonesViajeIniciado()

                cobrarComisiones()
                viewModel?.preguntarNavegador(this,destinoOtrosMapas)

                if (destinationLatLng != null) {
                    isStartedTrip = true
                    googleMaps?.clear()
                    agregarMarca()
                    dibujarRuta(destinationLatLng!!)
                    markerOrigin?.remove()
                    addDestinationMarker()

                    sonidos.crearTonoBuenViaje()
                    viewModel?.showButtonFinish(binding,this,destinoOtrosMapas)
                }
                binding?.btnHellegado?.visibility=View.GONE
            }else{
                Toast.makeText(this, "Error de conexion", Toast.LENGTH_LONG).show()
                Log.d("Booking", "ERROR: ${booking}")
                getBooking()
            }
        }
    }

    private fun mostrarBotonesViajeIniciado() {
        binding?.LinearCompartirRuta?.visibility=View.VISIBLE
        destinoOtrosMapas=destinationLatLng  //cambiamos el destino disponible en otros navegadores
//        binding.tiempoRecorrido.visibility=View.VISIBLE
//        startTimer()
    }

    private fun cobrarComisiones() {

            val comision=booking?.tarifas?.comisionApp
            val porcentaje= Utilidades.calcularPorcentaje(
                booking?.tarifas?.totalViaje!!,
                booking?.tarifas?.porcenajeComisionApp!!
            )
            val descuento= comision!! +porcentaje

            val driverProvider=DriverProvider()
            driverProvider.descontarComisionesApp(descuento).addOnCompleteListener {
                if(it.isSuccessful){
                  crearRegistroTransaccion(descuento)
                }
            }

    }

    private fun crearRegistroTransaccion(descuento: Double) {

        val datosRecarga = TransaccionesModelo(
            id = idTransaccion,
            idDriver =  DriverProvider.datosConductor!!.id,
            banco = "ixi-cobros",
            cuenta =  idTransaccion?.substring(idTransaccion?.length!! - 6),
            codigoVerificacion = "",
            valor = "-$descuento",
            estado = "Realizado",
            fecha = Date()
        )
        val transaccionesProvider=TransaccionesProvider()
        transaccionesProvider.create(datosRecarga)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("Transacciones", "Registro de firebase creado")
                } else {
                    Log.e("Transacciones", "Error en firebase")
                    crearRegistroTransaccion(descuento)
                }
            }


    }


    private fun updateToFinish() {


        isStartedTrip = false
        easyWayLocation?.endUpdates()
        geoProveedor.eliminarLocalizacionTrabajo(DriverProvider.datosConductor?.id!!)

        bookingAceptedsProvider.updateStatus(booking?.idDriver!!, "Finalizado").addOnCompleteListener {

            if (it.isSuccessful) {
                goToCalificationClient()
                val history=createHistory()
                historyProvider.create(history)
            }

        }
    }

    private fun goToCalificationClient() {
        val i = Intent(this, CalificationClientActivity::class.java)
        i.putExtra("price", booking!!.tarifas?.totalViaje)
        i.putExtra("origin", booking!!.origin)
        i.putExtra("destination", booking!!.destination)

        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)

        bookingAceptedsProvider.deleteBookingsForDriver(DriverProvider.datosConductor?.id!!)
    }

    val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this).apply {
            setMessage("Conectando Conductor...")
            setCancelable(false)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
        }
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
        directionUtil.drawPath(WAY_POINT_TAG)
    }

    private fun dibujarRuta(position:LatLng){

        if(ubicacionLatLong!=null && position!=null) {
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
    }




    private fun getClientInfo() {
        clientProvider.getClientById(booking?.idClient!!).addOnSuccessListener { document ->
            if (document.exists()) {
                client = document.toObject(Client::class.java)
            }
        }
    }

    fun detenerServico() {
        val serviceManager = ServiceLocalizationManager.getInstance()
        serviceManager.stopLocationService(this)//detener servicio de localizacion en segundo plano
    }
}