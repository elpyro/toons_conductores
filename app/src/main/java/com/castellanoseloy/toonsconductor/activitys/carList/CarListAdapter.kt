package com.castellanoseloy.toonsconductor.activitys.carList

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.modelos.CarModel
import com.castellanoseloy.toonsconductor.services.DatosPersitidos

class CarListAdapter  (
    private var car: MutableList<CarModel>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<CarListAdapter.CarViewHolder>() {

    interface OnItemClickListener {
        fun onSwitchClick(car: CarModel)
        fun onCarItemClick(car: CarModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        // Inflar el diseño del item_producto para crear la vista del ViewHolder
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car, parent, false)
        return CarViewHolder(view)
    }
    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        // Vincular los datos del producto con la vista del ViewHolder
        holder.bind(car[position])


    }

    override fun getItemCount(): Int {
        return car.size
    }


    inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val placaYModelo: TextView = itemView.findViewById(R.id.textview_placaymodelo)
        private val carImagen:ImageView= itemView.findViewById(R.id.imageView_car)
        private val estado: TextView = itemView.findViewById(R.id.textViewEstado)
        private val cambiar: ImageView = itemView.findViewById(R.id.imageView_cambiar)
        private val linearLayoutCar:LinearLayout=itemView.findViewById(R.id.linearLayoutCar)

        @SuppressLint("SetTextI18n")
        fun bind(car: CarModel) {


            placaYModelo.text = car.placa +" "+car.bland
            car.color?.let { carImagen.setColorFilter(it) }
            estado.text=car.estado

            if(DatosPersitidos.datosConductor?.vehiculo.equals(car.id)){
                cambiar.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_carro_48))
            }else{
                cambiar.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_cambiar))
            }

            cambiar.setOnClickListener {
                // Llamar a la función onItemClick del listener cuando se presiona la imagen
                itemClickListener.onSwitchClick(car)
            }

            linearLayoutCar.setOnClickListener {
                // Llamar a un método de la actividad CarList cuando se hace clic en linearLayoutCar
                (itemView.context as? CarList)?.onCarItemClick(car)
            }


        }
    }
}