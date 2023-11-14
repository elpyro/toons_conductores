package com.castellanoseloy.toonsconductor.activitys.saldo.recargaNequi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonscliente.objetos.Utilidades.generarMessageID
import com.castellanoseloy.toonsconductor.activitys.saldo.Saldo
import com.castellanoseloy.toonsconductor.api.NequiOAuth2Manager
import com.castellanoseloy.toonsconductor.api.NequiVerificacionPago
import com.castellanoseloy.toonsconductor.databinding.ActivityInformacionNequiBinding
import com.castellanoseloy.toonsconductor.proveedor.RecargasProvider
import com.optic.uberclonedriverkotlin.providers.DriverProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject

class InformacionNequi : AppCompatActivity() {
    private var recarga: String? = null
    private var codigoVerificacion: String? = null
    private var telefono: String? = null
    private lateinit var binding: ActivityInformacionNequiBinding
    private var nequiVerificacionPago = NequiVerificacionPago()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformacionNequiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent

        // Recuperar los valores pasados como extras
        telefono = intent.getStringExtra("telefono")
        codigoVerificacion = intent.getStringExtra("codigoVerificacion")
        recarga = intent.getStringExtra("recarga")

         verificacion()

        binding.botonAceptar.setOnClickListener { cerrar() }
    }

    private fun verificacion() {
        val recargasProvider=RecargasProvider()
        recargasProvider.buscarRecagasPorIdDriver(DriverProvider.datosConductor?.id!!, true){
            Log.d("Pruebas nequi","Las recargas se estan verificando")
        }
    }

    private fun cerrar() {
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}