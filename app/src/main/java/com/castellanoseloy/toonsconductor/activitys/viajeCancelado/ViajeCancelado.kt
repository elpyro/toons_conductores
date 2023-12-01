package com.castellanoseloy.toonsconductor.activitys.viajeCancelado

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import com.castellanoseloy.toonsconductor.activitys.mapActivity.MapaActivity
import com.castellanoseloy.toonsconductor.activitys.utils.Sonidos
import com.castellanoseloy.toonsconductor.databinding.ActivityViajeCanceladoBinding
import com.castellanoseloy.toonsconductor.proveedor.BookingAceptedsProvider
import com.castellanoseloy.toonsconductor.proveedor.HistoriesCancelProvider
import com.castellanoseloy.toonsconductor.services.DatosPersitidos
import com.optic.uberclonedriverkotlin.models.History

class ViajeCancelado : AppCompatActivity() {
    private var history: History? = null
    private lateinit var binding: ActivityViajeCanceladoBinding
    private var sonidos= Sonidos(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityViajeCanceladoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val extraId = intent.getStringExtra("id")

        Log.d("Viaje cancelado","Se ha recibido el id $extraId")
        sonidos.crearTonoCancelado()

        cargarHistorialCancelado(extraId)

        MapaActivity.tiempoActivo=0L // marcamos el tiempo inferior para que este de primero en la cola

        binding.btnAceptarCancelado.setOnClickListener { guardarComentario() }
    }

    private fun guardarComentario() {
        val comentario=binding.editTextComentarioDeConductor.text.toString()
        if(comentario.isNotEmpty()) {
            val historiesCancelProvider = HistoriesCancelProvider()
            history?.id?.let { historiesCancelProvider.updateComentarioCancelado(it,comentario) }
        }
        val i = Intent(this, MapaActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun cargarHistorialCancelado(id: String?) {
        Log.d("Viaje cancelado","buscanco historial  $id")
        val historiesCancelProvider=HistoriesCancelProvider()
        historiesCancelProvider.getHistoryCancelById(id!!).addOnSuccessListener { document ->
            if (document.exists()) {
                history = document.toObject(History::class.java)

                //usamos LinkMovementMethod para que reconosca los saltos de linea <br>
                binding.textViewMotivoConductor.text = Html.fromHtml(history?.commentDriver?:"")
                binding.textViewMotivoConductor.movementMethod = LinkMovementMethod.getInstance()

                binding.textViewMotivoCliente.text = Html.fromHtml(history?.commentClient?:"")
                binding.textViewMotivoCliente.movementMethod = LinkMovementMethod.getInstance()

                if(history?.commentClient!=null){
                    binding.editTextComentarioDeConductor.visibility=View.VISIBLE
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        val bookingAceptedsProvider=BookingAceptedsProvider()
        bookingAceptedsProvider.deleteBookingsForDriver(DatosPersitidos.datosConductor?.id!!)
    }

}