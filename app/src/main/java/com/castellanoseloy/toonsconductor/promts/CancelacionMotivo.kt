package com.castellanoseloy.toonsconductor.promts

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Spinner
import com.castellanoseloy.toonscliente.objetos.Utilidades

import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.proveedor.BookingAceptedsProvider
import com.castellanoseloy.toonsconductor.proveedor.HistoriesCancelProvider
import com.castellanoseloy.toonsconductor.services.DatosPersitidos.Companion.datosConductor
import com.castellanoseloy.toonsconductor.utils.NotificarEstados
import com.optic.uberclonedriverkotlin.models.Client
import com.optic.uberclonedriverkotlin.models.History


class CancelacionMotivo {

    fun mensaje(
        context: Context,
        history: History,
        client: Client?
    ) {

        val dialogBuilder = AlertDialog.Builder(context)

        // Inflar el layout para el diálogo
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.promt_cancelar_motivo, null)
        dialogBuilder.setView(dialogView)

        val detalle = dialogView.findViewById<EditText>(R.id.textFieldDetalles)
        val spinner= dialogView.findViewById<Spinner>(R.id.spinner_cancelaciones)



        // Configurar el botón "Aceptar"
        dialogBuilder.setPositiveButton("Aceptar") { dialogInterface, i ->
            val motivo=spinner.selectedItem.toString()+ " <br> "+detalle.text.toString().trim()
            history.commentDriver = motivo

            val bookingCancelProvider = HistoriesCancelProvider()
            bookingCancelProvider.create(history)
                .addOnCompleteListener {

                    var bookingAceptedsProvider = BookingAceptedsProvider()
                    bookingAceptedsProvider.updateStatus(
                        datosConductor!!.id!!,
                        "Cancelado"
                    ).addOnCompleteListener {

                        val notificarEstados = NotificarEstados()
                        notificarEstados.sendNotification(
                            "El conductor a cancelado el viaje del pasajero ${history.pasajero}",
                            client!!,
                            context
                        )

                        if (!it.isSuccessful) {
                            Utilidades.mostrarToast("Problemas de conexion", context)
                        }
                    }
                }


        }
        dialogBuilder.setNegativeButton("Volver") {dialogInterface, i ->

        }


// Mostrar el diálogo
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

}

