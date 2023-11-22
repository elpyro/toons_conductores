package com.castellanoseloy.toonsconductor.activitys.historiesDetails


import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMonenda
import com.castellanoseloy.toonsconductor.databinding.ActivityHistoriesDetailsBinding
import com.castellanoseloy.toonsconductor.promts.DatosReferidorPromt
import com.castellanoseloy.toonsconductor.promts.MostrarTarifas
import com.castellanoseloy.toonsconductor.proveedor.ClienteProveedor
import com.castellanoseloy.toonsconductor.utils.RelativeTime
import com.optic.uberclonedriverkotlin.models.Client

import com.optic.uberclonedriverkotlin.models.History

import com.optic.uberclonedriverkotlin.providers.HistoryProvider


class HistoriesDetailActivity : AppCompatActivity() {
    private var history: History? = null
    private lateinit var binding: ActivityHistoriesDetailsBinding
    private var historyProvider = HistoryProvider()
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
                binding.textViewPasajero.text=history?.pasajero?: "Desconocido"

                binding.textViewPrice.text = history!!.tarifas?.totalComisionConductor.toString().formatoMonenda()
                binding.textViewMyCalification.text = "${history?.calificationToDriver  ?: "5"}    ${history?.commentClient  ?: "El cliente no califico"}"
                binding.textViewClientCalification.text = "${history?.calificationToClient ?: "5"}    ${history?.commentDriver ?: "No calificaste"}"

                binding.textViewTimeAndDistance.text =
                    "${history?.time} Min - ${String.format("%.1f", history?.km)} Km"

                binding?.buttonVerCliente?.setOnClickListener{
                    mostrarCliente(history?.idClient)
                }
            }

        }
    }

    private fun mostrarCliente(idClient: String?) {
        var clienteProveedor = ClienteProveedor()
        clienteProveedor.getClientById(idClient!!).addOnSuccessListener { document ->
            if (document.exists()) {
                val cliente = document.toObject(Client::class.java)

                var datosRefeidorPromt= DatosReferidorPromt()
                datosRefeidorPromt.clienteDetalle(this,cliente!!)


                Log.d("Firestore", "Obteniendo datos del cliente ${cliente}")

            } else {
                Log.e("Firestore", "No se encontraron datos del cliente")
            }
        }
    }


}