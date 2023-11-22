package com.castellanoseloy.toonsconductor.promts

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.castellanoseloy.toonscliente.objetos.Utilidades

import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.modelos.BookingModel
import com.castellanoseloy.toonsconductor.proveedor.BookingAceptedsProvider
import com.castellanoseloy.toonsconductor.proveedor.HistoriesCancelProvider
import com.optic.uberclonedriverkotlin.models.History
import com.optic.uberclonedriverkotlin.providers.DriverProvider
import java.util.Date


class CancelacionMotivo {

    fun mensaje(
        context: Context,
        history: History
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
            val motivo=spinner.selectedItem.toString()+ "\\n"+detalle.text.toString().trim()
            history.commentDriver = motivo

            val bookingCancelProvider = HistoriesCancelProvider()
            bookingCancelProvider.create(history)
                .addOnCompleteListener {

                    var bookingAceptedsProvider = BookingAceptedsProvider()
                    bookingAceptedsProvider.updateStatus(
                        DriverProvider!!.datosConductor!!.id!!,
                        "Cancelado"
                    ).addOnCompleteListener {

                        if (!it.isSuccessful) {
                            Utilidades.mostrarToast("Problemas de conexion", context)
                        }
                    }
                }


        }

// Mostrar el diálogo
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

}

