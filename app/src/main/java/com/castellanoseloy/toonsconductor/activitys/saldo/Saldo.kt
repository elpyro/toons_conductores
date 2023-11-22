package com.castellanoseloy.toonsconductor.activitys.saldo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMoneda

import com.castellanoseloy.toonsconductor.activitys.saldo.recargaNequi.RecargaNequi
import com.castellanoseloy.toonsconductor.adapter.RecargasAdapter
import com.castellanoseloy.toonsconductor.databinding.ActivitySaldoBinding
import com.castellanoseloy.toonsconductor.modelos.TransaccionesModelo
import com.castellanoseloy.toonsconductor.proveedor.TransaccionesProvider

import com.optic.uberclonedriverkotlin.providers.DriverProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Saldo : AppCompatActivity() {

    private lateinit var adapter: RecargasAdapter
    private lateinit var binding: ActivitySaldoBinding
    var cargarRapida=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySaldoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerRecargas.layoutManager = linearLayoutManager



        listeners()
    }

    override fun onResume() {
        super.onResume()
        cargarRecargas()
    }

    private fun cargarRecargas() {
        val transaccionesProvider=TransaccionesProvider()

        transaccionesProvider.buscarTransaccionesPorIdDriver(DriverProvider.datosConductor?.id!!, false) { snapshot ->
                if(snapshot.isNotEmpty()){
                    adapter = RecargasAdapter(this, snapshot as ArrayList<TransaccionesModelo>)
                    binding.recyclerRecargas.adapter = adapter
                    mostrarSaldo()
                }
        }
    }

    private fun listeners() {
        binding?.botonRecargar?.setOnClickListener{ abrirRecargas()}
    }

    private fun abrirRecargas() {
        val i = Intent(this, RecargaNequi::class.java)
        startActivity(i)
    }



    private fun mostrarSaldo() {

        GlobalScope.launch(Dispatchers.IO) {
            if (!cargarRapida) {
                Utilidades.esperarUnSegundo()
                Utilidades.esperarUnSegundo()
                Utilidades.esperarUnSegundo()
            }
            cargarRapida = false

            // Actualizar el saldo en la interfaz de usuario (asegúrate de que sea en el hilo principal)
            withContext(Dispatchers.Main) {
                if (DriverProvider.datosConductor?.saldo != null) {
                    binding.saldoActual.text =
                        DriverProvider.datosConductor?.saldo.toString().formatoMoneda()
                }
            }
        }
    }



}