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
    private var  nequiOAuth2Manager = NequiOAuth2Manager()
    private var nequiVerificacionPago= NequiVerificacionPago()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformacionNequiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent

        // Recuperar los valores pasados como extras
         telefono = intent.getStringExtra("telefono")
         codigoVerificacion = intent.getStringExtra("codigoVerificacion")
         recarga = intent.getStringExtra("recarga")

//         verificacion()

        binding.botonAceptar.setOnClickListener { cerrar() }
    }

    private fun verificacion() {

                nequiVerificacionPago.verificarEstadoDePago(codigoVerificacion!!){ estado->
                   if(estado.isNotEmpty()) mostrarToastSegunStatusCode(estado)
                }


    }

    private fun cerrar() {
        val i = Intent(this, Saldo::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    fun mostrarToastSegunStatusCode(statusCode: String) {
        Log.d("Nequi", "el codigo del estado es $statusCode")
        when (statusCode) {

            "33" -> {
                // Pendiente
                runOnUiThread {
                    Toast.makeText(applicationContext, "Pago pendiente", Toast.LENGTH_SHORT).show()
                }
            }
            "34" -> {
                // Cancelado o rechazado
                runOnUiThread {
                    Toast.makeText(applicationContext, "Pago cancelado", Toast.LENGTH_SHORT).show()
                    anctualizarEstado("Cancelado")
                }
            }
            "35" -> {
                // Realizado
                runOnUiThread {
                    Toast.makeText(applicationContext, "Pago realizado", Toast.LENGTH_SHORT).show()
                    anctualizarEstado("Realizado")
                }
            }
            "69" -> {
                // Caducada
                runOnUiThread {
                    Toast.makeText(applicationContext, "Pago caducado", Toast.LENGTH_SHORT).show()
                    anctualizarEstado("Caducado")
                }
            }
            "71" -> {
                // Fallida
                runOnUiThread {
                    Toast.makeText(applicationContext, "Pago fallido", Toast.LENGTH_SHORT).show()
                    anctualizarEstado("Fallido")
                }
            }
            else -> {
                // Otro estado (manejar según sea necesario)
                runOnUiThread {
                    Toast.makeText(applicationContext, "Estado desconocido: $statusCode", Toast.LENGTH_SHORT).show()
                    anctualizarEstado("Desconocido")
                }
            }
        }
    }
    fun anctualizarEstado(estado:String){
        val recargasProvider=RecargasProvider()
        recargasProvider.buscarPorCodigoVerificacion(codigoVerificacion!!){it->
            recargasProvider.cambiarEstado(it.id!!,estado)
        }
    }

}