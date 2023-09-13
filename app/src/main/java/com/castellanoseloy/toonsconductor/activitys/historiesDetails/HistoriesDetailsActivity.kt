package com.castellanoseloy.toonsconductor.activitys.historiesDetails


import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMonenda
import com.castellanoseloy.toonsconductor.databinding.ActivityHistoriesDetailsBinding
import com.castellanoseloy.toonsconductor.proveedor.ClienteProveedor
import com.castellanoseloy.toonsconductor.utils.RelativeTime
import com.optic.uberclonedriverkotlin.models.Client

import com.optic.uberclonedriverkotlin.models.History
import com.optic.uberclonedriverkotlin.providers.DriverProvider

import com.optic.uberclonedriverkotlin.providers.HistoryProvider


class HistoriesDetailActivity : AppCompatActivity() {
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

    }

    private fun getHistory() {
        historyProvider.getHistoryById(extraId).addOnSuccessListener { document ->

            if (document.exists()) {
                val history = document.toObject(History::class.java)
                binding.textViewOrigin.text = history?.origin
                binding.textViewDestination.text = history?.destination
                binding.textViewDate.text =
                    RelativeTime.getTimeAgo(history?.timestamp!!, this@HistoriesDetailActivity)
                binding.textViewPrice.text = history.tarifas?.totalViaje.toString().formatoMonenda()
                binding.textViewMyCalification.text = "${history?.calificationToDriver}    ${history?.commentClient}"
                binding.textViewClientCalification.text = "${history?.calificationToClient}    ${history?.commentDriver}"
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
//                binding.textViewEmail.text = client?.email
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