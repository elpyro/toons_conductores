package com.castellanoseloy.toonsconductor.adapter

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMonenda
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.codigoMensajeVerificacion.CodigoMensajeVerificacion
import com.castellanoseloy.toonsconductor.activitys.historiesDetails.HistoriesDetailActivity
import com.castellanoseloy.toonsconductor.api.NequiVerificacionPago
import com.castellanoseloy.toonsconductor.modelos.RecargasModelo
import com.castellanoseloy.toonsconductor.proveedor.RecargasProvider
import com.castellanoseloy.toonsconductor.utils.RelativeTime


class RecargasAdapter(val context: Activity, val recargas: ArrayList<RecargasModelo>): RecyclerView.Adapter<RecargasAdapter.HistoriesAdapterViewHolder>() {
    val recargasOrdenadas = recargas.sortedByDescending  { it.fecha }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriesAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recarga, parent, false)
        return HistoriesAdapterViewHolder(view)
    }

    // ESTABLECER LA INFORMACION
    override fun onBindViewHolder(holder: HistoriesAdapterViewHolder, position: Int) {

        val recarga =  recargasOrdenadas[position] // UN SOLO HISTORIAL
        holder.textview_valor.text = recarga.valor?.formatoMonenda()
        holder.textViewEstado.text = recarga.estado

        if (recarga.fecha != null) {
            holder.textViewFecha.text = RelativeTime.getTimeAgo(recarga.fecha?.time!!, context)
        }


    }


    // EL TAMAñO DE LA LISTA QUE VAMOS A MOSTRAR
    override fun getItemCount(): Int {
        return recargasOrdenadas.size
    }


    class HistoriesAdapterViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val textview_valor: TextView
        val textViewFecha: TextView
        val textViewEstado: TextView

        init {
            textview_valor = view.findViewById(R.id.textview_valor)
            textViewFecha = view.findViewById(R.id.textViewFecha)
            textViewEstado = view.findViewById(R.id.textViewEstado)
        }

    }

}