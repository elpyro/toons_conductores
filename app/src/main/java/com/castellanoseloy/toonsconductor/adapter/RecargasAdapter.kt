package com.castellanoseloy.toonsconductor.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMonenda
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.historiesDetails.HistoriesDetailActivity
import com.castellanoseloy.toonsconductor.modelos.TransaccionesModelo
import com.castellanoseloy.toonsconductor.utils.RelativeTime


class RecargasAdapter(val context: Activity, recargas: ArrayList<TransaccionesModelo>): RecyclerView.Adapter<RecargasAdapter.HistoriesAdapterViewHolder>() {
    val recargasOrdenadas = recargas.sortedByDescending  { it.fecha }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriesAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recarga, parent, false)
        return HistoriesAdapterViewHolder(view)
    }

    // ESTABLECER LA INFORMACION
    override fun onBindViewHolder(holder: HistoriesAdapterViewHolder, position: Int) {


        val transaccion =  recargasOrdenadas[position] // UN SOLO HISTORIAL

        // Cambia el signo del valor si el banco es "ixi-cobros"
         if (transaccion.banco == "ixi-cobros") {
            holder.textviewValor.setTextColor(ContextCompat.getColor(context, R.color.rojo))
             holder.itemView.setOnClickListener { goToDetail(transaccion?.id!!) }
        } else {
            holder.textviewValor.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        holder.textviewValor.text = transaccion.valor?.formatoMonenda() +" | "+ transaccion.cuenta
        holder.textViewEstado.text = transaccion.estado

        when (transaccion.banco) {
            "ixi-cobros" -> holder.imageView_logo.setImageResource(R.drawable.ic_ixi_logo)
            "Nequi" -> holder.imageView_logo.setImageResource(R.drawable.nequi_logo)
            // Agrega más casos según sea necesario para otros bancos
            else -> holder.imageView_logo.setImageResource(R.drawable.ic_ixi_logo)
        }

        if (transaccion.estado.equals("Realizado", ignoreCase = true)) {
            holder.textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.verde))
        } else if(transaccion.estado.equals("Pendiente")) {
            holder.textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.rojo))
        }
        else {
            // Restaurar el color predeterminado en caso contrario
            holder.textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        if (transaccion.fecha != null) {
            holder.textViewFecha.text = RelativeTime.getTimeAgo(transaccion.fecha.time, context)
        }

    }

    private fun goToDetail(idHistory: String) {
        val i = Intent(context, HistoriesDetailActivity::class.java)
        i.putExtra("id", idHistory)
        context.startActivity(i)
    }

    // EL TAMAñO DE LA LISTA QUE VAMOS A MOSTRAR
    override fun getItemCount(): Int {
        return recargasOrdenadas.size
    }


    class HistoriesAdapterViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val textviewValor: TextView
        val textViewFecha: TextView
        val textViewEstado: TextView
        val  imageView_logo:ImageView
        init {
            textviewValor = view.findViewById(R.id.textview_valor)
            textViewFecha = view.findViewById(R.id.textViewFecha)
            textViewEstado = view.findViewById(R.id.textViewEstado)
            imageView_logo= view.findViewById(R.id.imageView_logo)
        }

    }

}