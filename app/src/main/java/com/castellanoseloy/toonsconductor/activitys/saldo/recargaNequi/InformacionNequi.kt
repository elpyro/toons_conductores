package com.castellanoseloy.toonsconductor.activitys.saldo.recargaNequi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.mapActivity.MapaActivity
import com.castellanoseloy.toonsconductor.activitys.saldo.Saldo
import com.castellanoseloy.toonsconductor.api.NequiOAuth2Manager
import com.castellanoseloy.toonsconductor.databinding.ActivityInformacionNequiBinding
import com.castellanoseloy.toonsconductor.databinding.ActivityRecargaNequiBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.Random

class InformacionNequi : AppCompatActivity() {
    private var recarga: String? = null
    private var codigoVerificacion: String? = null
    private var telefono: String? = null
    private lateinit var binding: ActivityInformacionNequiBinding
    private var  nequiOAuth2Manager = NequiOAuth2Manager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformacionNequiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent

        // Recuperar los valores pasados como extras
         telefono = intent.getStringExtra("telefono")
         codigoVerificacion = intent.getStringExtra("codigoVerificacion")
         recarga = intent.getStringExtra("recarga")

        binding.botonAceptar.setOnClickListener { crearToken() }
    }

    private fun crearToken() {
        nequiOAuth2Manager.getAccessToken {
            it?.let { it1 -> verificarEstadoDePago(it1) }
        }
    }

    private fun cerrar() {
        val i = Intent(this, Saldo::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    fun generarMessageID(): String {
        val random = Random()
        return (1000000000 + random.nextInt(900000000)).toString()
    }

    fun verificarEstadoDePago(accessToken: String) {
        val url = "https://api.sandbox.nequi.com/payments/v2/-services-paymentservice-getstatuspayment"
        val apiKey = nequiOAuth2Manager.apiKey
        val fecha=Utilidades.obtenerFechaActualEnFormatoISO8601()
        val mensajeId=generarMessageID()
        val jsonBody = """
    {
      "RequestMessage": {
        "RequestHeader": {
          "Channel": "PNP04-C001",
          "RequestDate": $fecha,
          "MessageID": "$mensajeId",
          "ClientID": ${nequiOAuth2Manager.clientId},
          "Destination": {
            "ServiceName": "PaymentsService",
            "ServiceOperation": "getStatusPayment",
            "ServiceRegion": "C001",
            "ServiceVersion": "1.0.0"
          }
        },
        "RequestBody": {
          "any": {
            "getStatusPaymentRQ": {
              "codeQR": "$codigoVerificacion"
            }
          }
        }
      }
    }
    """.trimIndent()

        val mediaType = "application/json".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType, jsonBody)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", accessToken)
            .addHeader("x-api-key", apiKey)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            // Procesa la respuesta JSON aquí
            Log.d("Pruebas Nequi","Respuesta exitosa $requestBody")
            val statusCode = obtenerStatusCode(responseBody!!)
            mostrarToastSegunStatusCode(statusCode)
        } else {
            // Maneja el error de conexión
            Log.e("Pruebas Nequi","Error en la respuesta ${response.message}")
            mostrarToastDeErrorDeConexion()
        }
    }

    fun obtenerStatusCode(responseBody: String): String {
        val jsonResponse = JSONObject(responseBody)
        val responseMessage = jsonResponse.optJSONObject("ResponseMessage")
        val responseHeader = responseMessage?.optJSONObject("ResponseHeader")
        return responseHeader?.optJSONObject("Status")?.optString("StatusCode", "-1") ?: "-1"
    }

    fun mostrarToastSegunStatusCode(statusCode: String) {
        Log.d("Nequi", "el codigo del estado es $statusCode")
        when (statusCode) {
            "0" -> {
                // Éxito
                runOnUiThread {
                    Toast.makeText(applicationContext, "Pago exitoso", Toast.LENGTH_SHORT).show()
                }
            }
            "33" -> {
                // Pendiente
                runOnUiThread {
                    Toast.makeText(applicationContext, "Pago pendiente", Toast.LENGTH_SHORT).show()
                }
            }
            "34" -> {
                // Cancelado o rechazado
                runOnUiThread {
                    Toast.makeText(applicationContext, "Pago cancelado o rechazado", Toast.LENGTH_SHORT).show()
                }
            }
            "35" -> {
                // Realizado
                runOnUiThread {
                    Toast.makeText(applicationContext, "Pago realizado", Toast.LENGTH_SHORT).show()
                }
            }
            "69" -> {
                // Caducada
                runOnUiThread {
                    Toast.makeText(applicationContext, "Pago caducado", Toast.LENGTH_SHORT).show()
                }
            }
            "71" -> {
                // Fallida
                runOnUiThread {
                    Toast.makeText(applicationContext, "Pago fallido", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                // Otro estado (manejar según sea necesario)
                runOnUiThread {
                    Toast.makeText(applicationContext, "Estado desconocido: $statusCode", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun mostrarToastDeErrorDeConexion() {
        runOnUiThread {
            Toast.makeText(applicationContext, "Error de conexión", Toast.LENGTH_SHORT).show()
        }
    }



}