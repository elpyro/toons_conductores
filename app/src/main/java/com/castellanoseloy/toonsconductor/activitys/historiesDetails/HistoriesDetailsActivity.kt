package com.castellanoseloy.toonsconductor.activitys.historiesDetails


import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMonenda
import com.castellanoseloy.toonsconductor.databinding.ActivityHistoriesDetailsBinding
import com.castellanoseloy.toonsconductor.promts.MostrarTarifas
import com.castellanoseloy.toonsconductor.proveedor.ClienteProveedor
import com.castellanoseloy.toonsconductor.utils.RelativeTime
import com.optic.uberclonedriverkotlin.models.Client

import com.optic.uberclonedriverkotlin.models.History
import com.optic.uberclonedriverkotlin.providers.DriverProvider

import com.optic.uberclonedriverkotlin.providers.HistoryProvider


class HistoriesDetailActivity : AppCompatActivity() {
    private var history: History? = null
    private lateinit var binding: ActivityHistoriesDetailsBinding
    private var historyProvider = HistoryProvider()
    private var clientProvider = ClienteProveedor()
    private var extraId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoriesDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Detalle del viaje"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.WHITE)

        extraId = intent.getStringExtra("id")!!
        getHistory()

        listeners()
    }

    private fun listeners() {
        binding.buttonVerDetalle.setOnClickListener{
            var mostrarTarifas= MostrarTarifas()
            mostrarTarifas.mostrarTarifas(this, history?.tarifas!!)
        }
    }

    private fun getHistory() {
        historyProvider.getHistoryById(extraId).addOnSuccessListener { document ->

            if (document.exists()) {
                history = document.toObject(History::class.java)
                binding.textViewOrigin.text = history?.origin
                binding.textViewDestination.text = history?.destination
                binding.textViewDate.text =
                    RelativeTime.getTimeAgo(history?.timestamp!!, this@HistoriesDetailActivity)
                binding.textViewPrice.text = history!!.tarifas?.totalViaje.toString().formatoMonenda()
                binding.textViewMyCalification.text = "${history?.calificationToDriver  ?: "5"}    ${history?.commentClient  ?: "El cliente no califico"}"
                binding.textViewClientCalification.text = "${history?.calificationToClient ?: "5"}    ${history?.commentDriver ?: "No calificaste"}"

                binding.textViewTimeAndDistance.text =
                    "${history?.time} Min - ${String.format("%.1f", history?.km)} Km"
                getClientInfo(history?.idClient!!)
            }

        }
    }

    private fun getClientInfo(id: String) {
//        clientProvider.getClientById(id).addOnSuccessListener { document ->
//            if (document.exists()) {
//                val client = document.toObject(Client::class.java)
//                binding.textViewName.text = "${client?.name} ${client?.lastname}"
//                if (client?.image != null) {
//                    if (client?.image != "") {
//                        Glide.with(this).load(client?.image).into(binding.circleImageProfile)
//                    }
//                }
//            }
//        }
    }
}