package com.castellanoseloy.toonsconductor.promts

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.R
import com.optic.uberclonedriverkotlin.models.Client
import com.optic.uberclonedriverkotlin.models.History

import com.optic.uberclonedriverkotlin.providers.HistoryProvider
import de.hdodenhof.circleimageview.CircleImageView
import java.text.DecimalFormat

class DatosRefeidorPromt {

    fun clienteDetalle(
        context: Context,
        cliente: Client,
    ) {
        val dialogBuilder = AlertDialog.Builder(context)

        // Inflar el layout para el diálogo
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.promt_informacion_referidor, null)
        dialogBuilder.setView(dialogView)

        val circleImageProfile=dialogView.findViewById<CircleImageView>(R.id.circleImageProfile)
        val textViewConductor = dialogView.findViewById<TextView>(R.id.textViewConductor)
        val TextView_telefono= dialogView.findViewById<TextView>(R.id.TextView_telefono)
        val imageViewWhatsappConductor=dialogView.findViewById<ImageView>(R.id.imageViewWhatsappConductor)
        val imageViewPhoneReferidor=dialogView.findViewById<ImageView>(R.id.imageViewPhoneReferidor)
        val textViewClientCalification=dialogView.findViewById<TextView>(R.id.textViewClientCalification)
        val textViewViajes=dialogView.findViewById<TextView>(R.id.textViewViajes)



        // Llamada a la función y manejo de los resultados
        obtenerCalificacionYViajes(cliente?.id!!) { promedio, cantidadRegistros ->

            val df = DecimalFormat("#.00")
            textViewClientCalification.text= df.format(promedio)
            textViewViajes.text= cantidadRegistros.toString()
        }

        textViewConductor.text=cliente.nombre?:""+cliente.apellido?:""
        TextView_telefono.text=cliente.telefono ?: ""


        if (cliente?.imagenPerfil != null) {
            if (cliente?.imagenPerfil != "") {
                try {
                    Glide.with(context).load(cliente?.imagenPerfil).into(circleImageProfile!!)
                }catch (e: Exception){  }

            }
        }

        imageViewPhoneReferidor.setOnClickListener { Utilidades.llamar(cliente?.telefono!!, context) }
        imageViewWhatsappConductor.setOnClickListener { Utilidades.enviarWhatsapp(cliente?.telefono!!, context) }

        dialogBuilder.setNegativeButton("Aceptar") { dialogInterface, i ->
            // No hacer nada
        }
// Mostrar el diálogo
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun obtenerCalificacionYViajes(idCliente: String, onComplete: (promedio: Double, cantidadRegistros: Int) -> Unit) {
        val histories = HistoryProvider()
        histories.getHistoriesClient(idCliente?:"").get().addOnSuccessListener { query ->

            if (query != null) {
                if (query.documents.size > 0) {
                    val documents = query.documents
                    var totalStar = 0.0
                    for (d in documents) {
                        val history = d.toObject(History::class.java)
                        totalStar += history?.calificationToClient ?: 5.0
                    }

                    val cantidadRegistros = documents.size
                    val promedio = if (cantidadRegistros > 0) totalStar / cantidadRegistros else 0.0

                    // Llamar al callback con los resultados
                    onComplete(promedio, cantidadRegistros)
                } else {
                    // No hay registros
                    onComplete(0.0, 0)
                }
            } else {
                // Error en la consulta
                onComplete(0.0, 0)
            }
        }
    }
}