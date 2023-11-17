package com.castellanoseloy.toonsconductor.fragments


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMonenda
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.modelos.Booking
import com.castellanoseloy.toonsconductor.proveedor.Autenticacion
import com.castellanoseloy.toonsconductor.proveedor.GeoProveedor
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.optic.uberclonedriverkotlin.models.Client
import de.hdodenhof.circleimageview.CircleImageView
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.promts.DatosRefeidorPromt
import com.castellanoseloy.toonsconductor.promts.MostrarTarifas
import com.castellanoseloy.toonsconductor.proveedor.ClienteProveedor


class ModalBottomTripInfo: BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ModalBottomTripInfo"
    }

    private var client: Client? = null
    private lateinit var booking: Booking

    var textViewClientName: TextView? = null
    var textViewOrigin: TextView? = null
    var textViewDestination: TextView? = null
    var textView_comision: TextView? = null
    var textView_total: TextView? = null
    var textViewReferidor: TextView? = null
    var textViewComentario: TextView? = null
    var textView_pasajeros: TextView? = null
    var textView_equipaje: TextView? = null
    var linearLayout_detalleCobro:LinearLayout? =null
    var linearLayout_referidor:LinearLayout?=null


//    var imageViewPhoneReferidor: ImageView? = null
    var imageViewPhone: ImageView? = null

    var circleImageReferidor: CircleImageView? = null

    val REQUEST_PHONE_CALL = 30


    private val clientProvider= ClienteProveedor()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_sheet_trip_info, container, false)
        textView_equipaje=view.findViewById(R.id.textView_equipaje)
        textView_pasajeros=view.findViewById(R.id.textView_pasajeros)
        textViewComentario=view.findViewById(R.id.textViewComentario)
        textViewReferidor=view.findViewById(R.id.textViewReferidor)

        textView_total=view.findViewById(R.id.textView_total)
        textView_comision= view.findViewById(R.id.textView_comision)
        textViewClientName = view.findViewById(R.id.textViewClientName)
        textViewOrigin = view.findViewById(R.id.textViewOrigin)
        textViewDestination = view.findViewById(R.id.textViewDestination)
        linearLayout_detalleCobro= view.findViewById(R.id.linearLayout_detalleCobro)
        linearLayout_referidor=view.findViewById(R.id.linearLayout_referidor)
//        imageViewPhoneReferidor = view.findViewById(R.id.imageViewPhoneReferidor)
        imageViewPhone = view.findViewById(R.id.imageViewPhone)

        circleImageReferidor = view.findViewById(R.id.circleImageReferidor)



        val data = arguments?.getString("booking")
        booking = Booking.fromJson(data!!)!!

        val comision = booking?.tarifas?.totalComisionReferido?.toDouble() ?: 0.0
        val precio = booking?.tarifas?.totalViaje?.toDouble() ?: 0.0

        textViewOrigin?.text = booking.origin
        textViewDestination?.text = booking.destination

        textView_equipaje?.text = booking.equipaje
        textView_pasajeros?.text = booking.pasajeros
        textViewComentario?.text = booking.comentario
        textView_comision?.text = comision.toString().formatoMonenda()
        textView_total?.text = precio.toString().formatoMonenda()

        textViewClientName?.text=booking.nombrePasajero

        getClientInfo()

        listeners()
        observadores()
        return view
    }


    private fun getClientInfo() {
        clientProvider.getClientById(booking.idClient!!).addOnSuccessListener { document ->
            if (document.exists()) {

                client = document.toObject(Client::class.java)
                Log.d("Firestore", "Datos del cliente: $client")
                textViewReferidor?.text = "${client?.nombre} ${client?.apellido}"

                if (client?.imagenPerfil != null) {
                    if (client?.imagenPerfil != "") {
                        try {
                            Glide.with(requireActivity()).load(client?.imagenPerfil).into(circleImageReferidor!!)
                        }catch (e: Exception){  }

                    }
                }

//                imageViewPhoneReferidor?.setOnClickListener {
//                    if (client?.telefono!! != null) {
//                        verificarPermisos(client?.telefono!!)
//                    }
//                }
                linearLayout_referidor?.setOnClickListener{
                    val datosRefeidorPromt=DatosRefeidorPromt()
                    client?.let { it1 -> datosRefeidorPromt.clienteDetalle(requireContext(), it1) }
                }

            }
        }
    }

    private fun verificarPermisos(telefono: String) {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_PHONE_CALL
            )
        }
        Utilidades.llamar(telefono, requireContext())
    }


    private fun observadores() {

    }


    private fun listeners() {

        linearLayout_detalleCobro?.setOnClickListener{
            var mostrarTarifas= MostrarTarifas()
            mostrarTarifas.mostrarTarifas(requireContext(), booking?.tarifas!!)
        }
        imageViewPhone?.setOnClickListener {
            if (booking?.telefonoPasajero != null) {
                verificarPermisos(booking?.telefonoPasajero!!)
            }
        }

    }




}

