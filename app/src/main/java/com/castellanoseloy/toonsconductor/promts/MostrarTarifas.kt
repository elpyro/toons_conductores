package com.castellanoseloy.toonsconductor.promts

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMoneda

import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.modelos.TarifasModelo


class MostrarTarifas {

    fun mostrarTarifas(
        context: Context,
        tarifas: TarifasModelo,

        ) {
        val dialogBuilder = AlertDialog.Builder(context)

        // Inflar el layout para el diálogo
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.promt_precios_viaje, null)
        dialogBuilder.setView(dialogView)
        val textViewTotalViaje = dialogView.findViewById<TextView>(R.id.editText_totalViaje)
        val textViewPorcentajeComisionReferido = dialogView.findViewById<TextView>(R.id.editText_porcentaje_comision_referido)
        val editText_porcentaje_comision_referido_etiqueta = dialogView.findViewById<TextView>(R.id.editText_porcentaje_comision_referido_etiqueta)
        val editText_TarifaFijaReferidor = dialogView.findViewById<TextView>(R.id.editText_TarifaFijaReferidor)
        val editText_porcentaje_comision_app_etiqueta = dialogView.findViewById<TextView>(R.id.editText_porcentaje_comision_app_etiqueta)
        val editText_porcentaje_comision_app = dialogView.findViewById<TextView>(R.id.editText_porcentaje_comision_app)
        val editText_tarifa_fija_app = dialogView.findViewById<TextView>(R.id.editText_tarifa_fija_app)
        val editText_ganancia_condutor = dialogView.findViewById<TextView>(R.id.editText_ganancia_condutor)

        textViewTotalViaje.setText(tarifas.totalViaje.toString().formatoMoneda())

        editText_TarifaFijaReferidor.setText(tarifas.tarifaComisionReferido.toString().formatoMoneda())

        editText_porcentaje_comision_app_etiqueta.setText("Comision ${tarifas.porcenajeComisionApp}%")
        val porcentaApp= (tarifas.totalViaje!! * tarifas.porcenajeComisionApp!!)/100
        editText_porcentaje_comision_app.setText(porcentaApp.toString().formatoMoneda())
        editText_tarifa_fija_app.setText(tarifas.comisionApp.toString().formatoMoneda())

        editText_ganancia_condutor.setText(tarifas.totalComisionConductor.toString().formatoMoneda())

        editText_porcentaje_comision_referido_etiqueta.setText("Comision ${tarifas.porcenajeComisionReferido}%")
        val comisionReferidor= (tarifas.totalViaje!! * tarifas.porcenajeComisionReferido!!)/100
        textViewPorcentajeComisionReferido.setText(comisionReferidor.toString().formatoMoneda())


// Configurar el botón "Aceptar"
        dialogBuilder.setPositiveButton("Aceptar") { dialogInterface, i ->

        }

// Mostrar el diálogo
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
}