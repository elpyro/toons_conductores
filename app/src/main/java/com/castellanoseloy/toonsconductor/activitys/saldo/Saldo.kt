package com.castellanoseloy.toonsconductor.activitys.saldo

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager

import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMonenda
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.historial.HistoriesActivity
import com.castellanoseloy.toonsconductor.activitys.mainActivity.MainActivity
import com.castellanoseloy.toonsconductor.activitys.registro_usuario.MyActivityViewModel
import com.castellanoseloy.toonsconductor.activitys.saldo.recargaNequi.RecargaNequi
import com.castellanoseloy.toonsconductor.adapter.RecargasAdapter
import com.castellanoseloy.toonsconductor.api.NequiOAuth2Manager
import com.castellanoseloy.toonsconductor.api.NequiPaymentCancellationManager
import com.castellanoseloy.toonsconductor.api.NequiPaymentManager
import com.castellanoseloy.toonsconductor.apiimport.NequiPaymentStatusManager
import com.castellanoseloy.toonsconductor.databinding.ActivityRegistroUsuarioBinding
import com.castellanoseloy.toonsconductor.databinding.ActivitySaldoBinding
import com.castellanoseloy.toonsconductor.modelos.RecargasModelo
import com.castellanoseloy.toonsconductor.objetos.BodyUtils
import com.castellanoseloy.toonsconductor.proveedor.RecargasProvider

import com.optic.uberclonedriverkotlin.providers.DriverProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

import java.util.Base64
import java.nio.charset.StandardCharsets

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Random

class Saldo : AppCompatActivity() {

    private lateinit var adapter: RecargasAdapter
    private lateinit var binding: ActivitySaldoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySaldoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerRecargas.layoutManager = linearLayoutManager

        mostrarSaldo()
        cargarRecargas()
        listeners()
    }

    private fun cargarRecargas() {
        val recargasProvider=RecargasProvider()

        recargasProvider.buscarPorIdDriver(DriverProvider.datosConductor?.id!!) { snapshot ->
                if(snapshot.isNotEmpty()){
                    adapter = RecargasAdapter(this, snapshot as ArrayList<RecargasModelo>)
                    binding.recyclerRecargas.adapter = adapter
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

//    private fun verificacion() {
//        var nequiOAuth2Manager=NequiOAuth2Manager()
//
//        runOnUiThread {
//            nequiOAuth2Manager.getAccessToken { accessToken ->
//                if (accessToken != null) {
//                    val codigo=binding.editTextCodigo.text.toString().trim()
//                   verificarEstadoDePago(accessToken,codigo)
//                } else {
//                    // Maneja el error de autenticación
//                    Log.e("Nequi", "Error de autenticacion")
//                }
//            }
//        }
//    }

//    private fun nequi() {
//        val recarga=binding.editTextRecarga.text.toString().trim()
//        if(recarga.isNullOrEmpty()) return
//
//        var nequiOAuth2Manager=NequiOAuth2Manager()
//
//        runOnUiThread {
//            nequiOAuth2Manager.getAccessToken { accessToken ->
//                if (accessToken != null) {
//                    // Aquí puedes usar el token de acceso
//                    Log.d("Nequi", "La recarga es  $recarga y El token de acceso es: $accessToken ")
//                    enviarPagoConNotificacionPush(accessToken,recarga )
//                } else {
//                    // Maneja el error de autenticación
//                    Log.e("Nequi", "Error de autenticacion")
//                }
//            }
//        }
//
//    }


    private fun mostrarSaldo() {
        if(DriverProvider.datosConductor?.saldo!=null) binding.saldoActual.text=DriverProvider.datosConductor?.saldo.toString().formatoMonenda()
    }

    fun generarMessageID(): String {
        val random = Random()
        return (1000000000 + random.nextInt(900000000)).toString()
    }

//    fun enviarPagoConNotificacionPush(accessToken:String, recarga:String) {
//        val url = "https://api.sandbox.nequi.com/payments/v2/-services-paymentservice-unregisteredpayment"
//        val apiKey = "vG7JvMu6laaBWviQIsrJaaKaeWNRHaX44dFtDfBr"
//
//        val messageID = generarMessageID() // Genera un MessageID aleatorio
//        runOnUiThread {
//            binding?.editTextCodigo?.setText(messageID)
//        }
//
//        Log.d("Nequi", "Id de la transaccion $messageID")
//        val jsonBody = """
//        {
//          "RequestMessage": {
//            "RequestHeader": {
//              "Channel": "PNP04-C001",
//              "RequestDate": "2017-06-21T20:26:12.654Z",
//              "MessageID": "$messageID",
//              "ClientID": "12345",
//              "Destination": {
//                "ServiceName": "PaymentsService",
//                "ServiceOperation": "unregisteredPayment",
//                "ServiceRegion": "C001",
//                "ServiceVersion": "1.2.0"
//              }
//            },
//            "RequestBody": {
//              "any": {
//                "unregisteredPaymentRQ": {
//                  "phoneNumber": "3720002148",
//                  "code": "NIT_1",
//                  "value": "$recarga",
//                  "reference1": "reference1",
//                  "reference2": "reference2",
//                  "reference3": "reference3"
//                }
//              }
//            }
//          }
//        }
//    """.trimIndent()
//
//        val mediaType = "application/json".toMediaTypeOrNull()
//        val requestBody = RequestBody.create(mediaType, jsonBody)
//
//        val request = Request.Builder()
//            .url(url)
//            .addHeader("Authorization", accessToken)
//            .addHeader("x-api-key", apiKey)
//            .post(requestBody)
//            .build()
//
//        val client = OkHttpClient()
//        val response = client.newCall(request).execute()
//
//        if (response.isSuccessful) {
//            val responseBody = response.body?.string()
//            val jsonResponse = JSONObject(responseBody)
//
//            val responseMessage = jsonResponse.optJSONObject("ResponseMessage")
//            val responseHeader = responseMessage?.optJSONObject("ResponseHeader")
//            val statusCode = responseHeader?.optJSONObject("Status")?.optString("StatusCode")
//
//            if (statusCode == "0") {
//                // Éxito
//                Log.d("Nequi", "Respuesta exitosa: $responseBody")
//                // Muestra un Toast para indicar el éxito
//                runOnUiThread {
//                    Toast.makeText(applicationContext, "Operación exitosa", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                // Error
//                Log.e("Nequi", "Error en la operación: $statusCode - ${responseHeader?.optString("StatusDesc")}")
//                // Muestra un Toast con el mensaje de error
//                runOnUiThread {
//                    Toast.makeText(applicationContext, "Error en la operación: $statusCode", Toast.LENGTH_SHORT).show()
//                }
//            }
//        } else {
//            Log.e("Nequi", "Error al enviar el pago con notificación Push: ${response.code} - ${response.message}")
//            // Muestra un Toast de error en la conexión
//            runOnUiThread {
//                Toast.makeText(applicationContext, "Error de conexión", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }


    fun verificarEstadoDePago(accessToken: String, codigo:String) {
        val url = "https://api.sandbox.nequi.com/payments/v2/-services-paymentservice-unregisteredpayment"
        val apiKey = "vG7JvMu6laaBWviQIsrJaaKaeWNRHaX44dFtDfBr"

        val jsonBody = """
    {
      "RequestMessage": {
        "RequestHeader": {
          "Channel": "PNP04-C001",
          "RequestDate": "2017-06-21T20:26:12.654Z",
          "MessageID": "$codigo",
          "ClientID": "12345",
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
              "codeQR": "350-913291938-2471-C001"
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
            val statusCode = obtenerStatusCode(responseBody!!)
            mostrarToastSegunStatusCode(statusCode)
        } else {
            // Maneja el error de conexión
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