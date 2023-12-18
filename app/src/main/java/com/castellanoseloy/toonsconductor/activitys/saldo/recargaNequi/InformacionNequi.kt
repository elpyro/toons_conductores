package com.castellanoseloy.toonsconductor.activitys.saldo.recargaNequi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.castellanoseloy.toonsconductor.api.NequiVerificacionPago
import com.castellanoseloy.toonsconductor.databinding.ActivityInformacionNequiBinding
import com.castellanoseloy.toonsconductor.proveedor.TransaccionesProvider
import com.castellanoseloy.toonsconductor.services.DatosPersitidos

class InformacionNequi : AppCompatActivity() {
    private var recarga: String? = null
    private var codigoVerificacion: String? = null
    private var telefono: String? = null
    private lateinit var binding: ActivityInformacionNequiBinding

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
        val transaccionesProvider=TransaccionesProvider()
        transaccionesProvider.buscarTransaccionesPorIdDriver(DatosPersitidos.datosConductor?.id!!, true){
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