package com.castellanoseloy.toonsconductor.activitys.calificationClientActivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMonenda
import com.castellanoseloy.toonsconductor.activitys.mapActivity.MapaActivity
import com.castellanoseloy.toonsconductor.activitys.utils.Sonidos
import com.castellanoseloy.toonsconductor.databinding.ActivityCalificationClientBinding
import com.optic.uberclonedriverkotlin.models.History
import com.optic.uberclonedriverkotlin.providers.HistoryProvider

class CalificationClientActivity : AppCompatActivity() {
    private var history: History? = null
    private lateinit var binding: ActivityCalificationClientBinding
    private var sonidos=Sonidos()
    private var historyProvider = HistoryProvider()
    private var calification = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificationClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
       // window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val extraPrice = intent.getDoubleExtra("price", 0.0)
        val extraOrigin = intent.getStringExtra("origin")
        val extraDestination = intent.getStringExtra("destination")
        val extraTime = intent.getDoubleExtra("time", 0.0)
        val extraKm = intent.getDoubleExtra("km", 0.0)

        binding.textViewPrice.text = extraPrice.toString().formatoMonenda()
        binding.textViewOrigin.text=extraOrigin
        binding.textViewDestination.text=extraDestination
        binding.textViewTimeAndDistance.text= "Min: ${String.format("%.1f", extraTime)}, Km: ${String.format("%.1f", extraKm)}"

        sonidos.crearTono(this)

        listeners()

        getHistory()
    }

    private fun listeners() {

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, value, b ->
            calification = value
        }

        binding.btnCalification.setOnClickListener{
            if (history?.id != null) {
                updateCalification(history?.id!!)
            }
            else {
                Toast.makeText(this, "El id del historial es nulo", Toast.LENGTH_LONG).show()
            }
        }

    }
    private fun updateCalification(idDocument: String) {
        val comment=binding.editTextComentarioDeConductor.text.toString().trim()
        historyProvider.updateCalificationToClient(idDocument, calification, comment).addOnCompleteListener {
            if (it.isSuccessful) {
                nuevoViaje()
            }
            else {
                Toast.makeText(this@CalificationClientActivity, "Error al actualizar la calificacion", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun nuevoViaje() {
        val i = Intent(this, MapaActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun getHistory() { //obtener el ultimo historial del conductor
        historyProvider.getLastHistory().get().addOnSuccessListener { query ->
            if (query != null) {

                if (query.documents.size > 0) {
                    history = query.documents[0].toObject(History::class.java)
                    history?.id = query.documents[0].id
                    Log.d("FIRESTORE", "Historial: ${history?.toJson()}")
                }
                else {
                    Toast.makeText(this, "No se encontro el historial", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}