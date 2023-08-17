package com.castellanoseloy.toonsconductor.activitys.calificationClientActivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMonenda
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.mapActivity.MapaActivity
import com.castellanoseloy.toonsconductor.activitys.utils.Sonidos
import com.castellanoseloy.toonsconductor.databinding.ActivityCalificationClientBinding

class CalificationClientActivity : AppCompatActivity() {
 //   private var history: History? = null
    private lateinit var binding: ActivityCalificationClientBinding
    private var sonidos=Sonidos()
//    private var historyProvider = HistoryProvider()
//    private var calification = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificationClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        var extraPrice = intent.getDoubleExtra("price", 0.0)
        var extraOrigin = intent.getStringExtra("origin")
        var extraDestination = intent.getStringExtra("destination")
        var extraTime = intent.getDoubleExtra("time", 0.0)
        var extraKm = intent.getDoubleExtra("km", 0.0)

        binding.textViewPrice.text = extraPrice.toString().formatoMonenda()
        binding.textViewOrigin.text=extraOrigin
        binding.textViewDestination.text=extraDestination
        binding.textViewTimeAndDistance.text= "Min: ${String.format("%.1f", extraTime)}, Km: ${String.format("%.1f", extraKm)}"

        sonidos.crearTono(this)
        listeners()
    }

    private fun listeners() {

        binding.btnCalification.setOnClickListener{
            nuevoViaje()
        }

    }

    private fun nuevoViaje() {
        val i = Intent(this, MapaActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

}