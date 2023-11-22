package com.castellanoseloy.toonsconductor.fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMonenda
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.mapActivity.MapaActivity
import com.castellanoseloy.toonsconductor.activitys.mapTripActivity.MapTripActivity
import com.castellanoseloy.toonsconductor.activitys.utils.Sonidos
import com.castellanoseloy.toonsconductor.modelos.BookingModel
import com.castellanoseloy.toonsconductor.proveedor.BookingAceptedsProvider
import com.castellanoseloy.toonsconductor.proveedor.BookingProvider
import com.castellanoseloy.toonsconductor.proveedor.CarProvider
import com.castellanoseloy.toonsconductor.proveedor.ClienteProveedor
import com.castellanoseloy.toonsconductor.proveedor.GeoProveedor
import com.castellanoseloy.toonsconductor.utils.NotificarEstados
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.optic.uberclonedriverkotlin.models.Client
import com.optic.uberclonedriverkotlin.providers.DriverProvider


class ModalBottomSheetBooking: BottomSheetDialogFragment() {
    companion object {
        const val TAG = "ModalBottomSheetBooking"
    }

    private lateinit var textViewTimeAndDistanceEntreCliente: TextView
    private lateinit var textViewSolicitudViaje:TextView
    private lateinit var textViewOrigin: TextView
    private lateinit var textViewDestination: TextView
    private lateinit var textViewTimeAndDistance: TextView
    private lateinit var textView_tarifa: TextView
    private lateinit var textView_pasajeros:TextView
    private lateinit var textView_equipaje:TextView
    private lateinit var textViewComentario:TextView
    private lateinit var btnAccept: Button
    private lateinit var btnCancel: Button

    private val bookingProvider = BookingProvider()
    private val bookingAceptedsProvider = BookingAceptedsProvider()
    private val geoProvider = GeoProveedor()
    private lateinit var sonidos: Sonidos
    private lateinit var booking: BookingModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_sheet_booking, container, false)

        sonidos = Sonidos(requireContext())

        textViewSolicitudViaje=view.findViewById(R.id.TextView_tiempo)
        textViewOrigin = view.findViewById(R.id.textViewOrigin)
        textViewDestination = view.findViewById(R.id.textViewDestination)
        textViewTimeAndDistance = view.findViewById(R.id.textViewTimeAndDistance)
        textViewTimeAndDistanceEntreCliente = view.findViewById(R.id.textViewTimeAndDistanceCliente)
        textView_tarifa = view.findViewById(R.id.textView_tarifa)
        textView_equipaje=view.findViewById(R.id.textView_equipaje)
        textView_pasajeros=view.findViewById(R.id.textView_pasajeros)
        textViewComentario=view.findViewById(R.id.textViewComentario)
        btnAccept = view.findViewById(R.id.btnAccept)
        btnCancel = view.findViewById(R.id.btnCancel)

        val data = arguments?.getString("booking")
        booking = BookingModel.fromJson(data!!)!!
        Log.d("ARGUMENTS", "Booking: ${booking?.toJson()}")

        (activity as? MapaActivity)?.modalMostrado=true
        textViewOrigin.text = booking?.origin
        textViewDestination.text = booking?.destination
        textView_pasajeros.text=booking.pasajeros
        textView_equipaje.text=booking.equipaje
        textViewComentario.text=booking.comentario
        textView_tarifa.text=booking?.tarifas?.totalComisionConductor.toString().formatoMonenda()

        textViewTimeAndDistance.text = "${String.format("%.1f", booking?.time)} Min - ${String.format("%.1f", booking?.km)} Km"

        val distanciaEntreCliente=(activity as MapaActivity).distanceString.toString()
        val tiempoEntreCliente=(activity as MapaActivity).timeString?.toString()
        textViewTimeAndDistanceEntreCliente.text = "$tiempoEntreCliente Min - $distanciaEntreCliente Km"

        sonidos.crearTonoInformativo()

        Log.d("Firestore", "Modal creado")

        listeners()
        observadores()
        return view
    }

    private fun observadores() {
        (activity as? MapaActivity)?.viewModel?.currentTimerValue!!.observe(this, Observer { value ->
            val seconds = value / 1000 // Convertir de milisegundos a segundos
            textViewSolicitudViaje.text = "SOLICITUD DE VIAJE ${seconds.toInt()} SEGUNDOS"
            Log.d("TIMER", "Current timer value changed: $seconds seconds")
        })
    }


    private fun listeners() {
        btnAccept.setOnClickListener { acceptBooking(booking?.idClient!!) }
        btnCancel.setOnClickListener { cancelBooking(booking?.idClient!!) }
    }

     fun cancelBooking(idClient: String) {
         bookingProvider.getBooking().get().addOnCompleteListener { task->
             if(task.isSuccessful){
                 val document = task.result

                 if (document.exists()) {
                     bookingProvider.updateStatus(idClient, "cancel").addOnCompleteListener { task ->
                         if (task.isSuccessful) {

                             Log.d("FIRESTORE", "booking actualizado a cancelado")

                             (activity as? MapaActivity)?.timer?.cancel()

                             ocultarModal()

                         }
                     }
                 }else{
                     ocultarModal()
                 }
             }else{
                 ocultarModal()
             }
         }
    }

    fun ocultarModal(){
        (activity as? MapaActivity)?.borrarRuta()
        val modalBooking = (activity as? MapaActivity)?.modalBooking
        if (modalBooking != null && modalBooking.isAdded) {
            modalBooking.dismiss()
        }
        (activity as? MapaActivity)?.modalMostrado=false
        Log.d("FIRESTORE", "Modal ocultado")
    }
    private fun acceptBooking(idClient: String) {
        bookingProvider.getBooking().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result

                if (document.exists()) {
                    bookingProvider.updateStatus(idClient, "accept").addOnCompleteListener {
                        (activity as? MapaActivity)?.timer?.cancel()
                        if (it.isSuccessful) {
                            (activity as? MapaActivity)?.viajeAceptado=true
                            (activity as? MapaActivity)?.detenerServico()
                            (activity as? MapaActivity)?.easyWayLocation?.endUpdates()
                            geoProvider.eliminarLocalizacion(DriverProvider.datosConductor?.id!!)

                            booking.id=Utilidades.generarIdUnico()
                            booking!!.status = "accept" // Crear el booking aceptado
                            booking.car= CarProvider.datosCarroEnUso //establecer el carro en uso

                            bookingAceptedsProvider.create(booking!!).addOnCompleteListener {
                                Log.d("FIRESTORE", "Viaje aceptado: ${booking}")
                                enviarNotificacion(booking.idClient!!,requireContext())
                                Utilidades.mostrarToast("Viaje Aceptado", requireContext())
                                goToMapTrip()
                            }
                        } else {
                            viajeRechazado()
                        }
                    }
                } else {
                    // El booking no existe
                    viajeRechazado()
                }
            } else {
                viajeRechazado()
            }
        }
    }

    private fun enviarNotificacion(idClient: String, context: Context) {

        val clienteProveedor= ClienteProveedor()
        clienteProveedor.getClientById(idClient).addOnSuccessListener { document ->
            if (document.exists()) {
                val client = document.toObject(Client::class.java)
                val nombreConductor= DriverProvider.datosConductor?.nombre +" "+DriverProvider.datosConductor?.apellido
                val notificarEstados= NotificarEstados()
                notificarEstados.sendNotification("$nombreConductor ha aceptado el viaje",
                    client!!,context)
            }
        }
    }


    private fun viajeRechazado() {
        Toast.makeText(activity, "No se pudo aceptar el viaje", Toast.LENGTH_LONG).show()
        (activity as? MapaActivity)?.borrarRuta()
        (activity as? MapaActivity)?.listenerBooking()
        ocultarModal()
    }

    private fun goToMapTrip() {
        val i = Intent(context, MapTripActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context?.startActivity(i)
    }

}