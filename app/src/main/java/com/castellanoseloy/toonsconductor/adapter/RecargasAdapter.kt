package com.castellanoseloy.toonsconductor.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMonenda
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.modelos.RecargasModelo
import com.castellanoseloy.toonsconductor.utils.RelativeTime


class RecargasAdapter(val context: Activity, recargas: ArrayList<RecargasModelo>): RecyclerView.Adapter<RecargasAdapter.HistoriesAdapterViewHolder>() {
    val recargasOrdenadas = recargas.sortedByDescending  { it.fecha }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriesAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recarga, parent, false)
        return HistoriesAdapterViewHolder(view)
    }

    // ESTABLECER LA INFORMACION
    override fun onBindViewHolder(holder: HistoriesAdapterViewHolder, position: Int) {

        val recarga =  recargasOrdenadas[position] // UN SOLO HISTORIAL
        holder.textviewValor.text = recarga.valor?.formatoMonenda() +" | "+ recarga.cuenta
        holder.textViewEstado.text = recarga.estado

        if (recarga.estado.equals("Realizado", ignoreCase = true)) {
            holder.textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.verde))
        } else if(recarga.estado.equals("Pendiente")) {
            holder.textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.rojo))
        }
        else {
            // Restaurar el color predeterminado en caso contrario
            holder.textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        if (recarga.fecha != null) {
            holder.textViewFecha.text = RelativeTime.getTimeAgo(recarga.fecha.time, context)
        }

    }


    // EL TAMAñO DE LA LISTA QUE VAMOS A MOSTRAR
    override fun getItemCount(): Int {
        return recargasOrdenadas.size
    }


    class HistoriesAdapterViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val textviewValor: TextView
        val textViewFecha: TextView
        val textViewEstado: TextView

        init {
            textviewValor = view.findViewById(R.id.textview_valor)
            textViewFecha = view.findViewById(R.id.textViewFecha)
            textViewEstado = view.findViewById(R.id.textViewEstado)
        }

    }

}