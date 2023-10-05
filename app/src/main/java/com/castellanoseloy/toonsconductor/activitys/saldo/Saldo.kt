package com.castellanoseloy.toonsconductor.activitys.saldo

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonscliente.objetos.Utilidades.formatoMonenda
import com.castellanoseloy.toonsconductor.R
import com.castellanoseloy.toonsconductor.activitys.mainActivity.MainActivity
import com.castellanoseloy.toonsconductor.activitys.registro_usuario.MyActivityViewModel
import com.castellanoseloy.toonsconductor.api.NequiOAuth2Manager
import com.castellanoseloy.toonsconductor.api.NequiPaymentCancellationManager
import com.castellanoseloy.toonsconductor.api.NequiPaymentManager
import com.castellanoseloy.toonsconductor.apiimport.NequiPaymentStatusManager
import com.castellanoseloy.toonsconductor.databinding.ActivityRegistroUsuarioBinding
import com.castellanoseloy.toonsconductor.databinding.ActivitySaldoBinding
import com.castellanoseloy.toonsconductor.objetos.BodyUtils

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

class Saldo : AppCompatActivity() {

    private lateinit var binding: ActivitySaldoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySaldoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mostrarSaldo()
        listeners()
    }

    private fun listeners() {
        binding?.botonRecargar?.setOnClickListener{nequi()}
    }

    private fun nequi() {
        var nequiOAuth2Manager=NequiOAuth2Manager()

        runOnUiThread {
            nequiOAuth2Manager.getAccessToken { accessToken ->
                if (accessToken != null) {
                    veficiarFechaVencimiento(accessToken)
                    // Aquí puedes usar el token de acceso
                    Log.d("Nequi", "El token de acceso es: $accessToken")
                    crearSolicitudPost(accessToken)
                    //cancelarSolicitud(accessToken)
 //                   chequearStatus(accessToken)
  //                  otrometodo()
                } else {
                    // Maneja el error de autenticación
                    println("Error de autenticación")
                }
            }
        }

    }

    private fun veficiarFechaVencimiento(accessToken: String) {
        val token =accessToken

        val parts = token.split(".")
        if (parts.size == 3) {
            val decodedPayload = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8)
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            val payloadJson = JSONObject(decodedPayload)
            val expirationTimeInSeconds = payloadJson.optLong("exp")

            if (expirationTimeInSeconds != null) {
                val expirationTimeMillis = expirationTimeInSeconds * 1000
                val expirationDateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(expirationTimeMillis),
                    ZoneId.systemDefault()
                )

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val formattedExpirationTime = formatter.format(expirationDateTime)
                Log.d("Nequi", "Fecha de vencimiento del token: $formattedExpirationTime")

            }
        }
    }

    private fun chequearStatus(accessToken: String) {
        val trnId = "M28" // Reemplaza con el trnId de la solicitud de pago que deseas consultar
        val paymentStatusManager = NequiPaymentStatusManager(accessToken)
        paymentStatusManager.checkPaymentStatus(trnId) { success, response ->
            if (success) {
                // Procesar la respuesta exitosa
                Log.d("Nequi", "Respuesta del estado de pago: $response")
            } else {
                // Manejar una respuesta no exitosa
                Log.e("Nequi", "Error al consultar el estado de pago")
            }
        }
    }

        private fun cancelarSolicitud(accessToken: String) {


            val paymentCancellationManager = NequiPaymentCancellationManager(accessToken)
            paymentCancellationManager.cancelPaymentNotification(
                "1",
                "3012146840",
                "350-3012146840-9232-8944090690"
            ) { success, response ->
                if (success) {
                    // Procesar la respuesta exitosa
                    println("Cancelación exitosa: $response")
                } else {
                    // Manejar la respuesta no exitosa
                    println("Error en la cancelación: $response")
                }
            }
        }




    private fun prueba(accessToken: String) {

            val client = OkHttpClient()

            // Construir el cuerpo de la solicitud en formato JSON
            val requestBody = JSONObject()
            val requestMessage = JSONObject()
            val requestHeader = JSONObject()
            val requestBodyData = JSONObject()

            requestHeader.put("Channel", "PNP04-C001")
            requestHeader.put("RequestDate", "2017-06-21T20:26:12.654Z")
            requestHeader.put("MessageID", "1215123684")
            requestHeader.put("ClientID", "12345")

            requestBodyData.put("phoneNumber", "3112972555")
            requestBodyData.put("code", "NIT_1")
            requestBodyData.put("value", "1000")
            requestBodyData.put("reference1", "reference1")
            requestBodyData.put("reference2", "reference2")
            requestBodyData.put("reference3", "reference3")

            requestMessage.put("RequestHeader", requestHeader)
            requestMessage.put("RequestBody", JSONObject().put("any", JSONObject().put("unregisteredPaymentRQ", requestBodyData)))

            requestBody.put("RequestMessage", requestMessage)

            // Crear la solicitud POST
            val request = Request.Builder()
                .url("https://api.sandbox.nequi.com/payments/v2/-services-paymentservice-unregisteredpayment")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer $accessToken") // Reemplaza con tu token
                .header("x-api-key", "vG7JvMu6laaBWviQIsrJaaKaeWNRHaX44dFtDfBr")
                .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody.toString()))
                .build()

            // Realizar la solicitud de forma asíncrona
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Manejar el fallo de la solicitud
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        // Procesar la respuesta exitosa
                        val responseBody = response.body?.string()
                        runOnUiThread {
                            Log.d("Nequi", "Respuesta del servidor: $response")
                            // Actualizar la interfaz de usuario con los datos de la respuesta
                            // responseBody contiene la respuesta JSON
                        }
                    } else {
                        // Manejar una respuesta no exitosa (código de estado HTTP no 200)
                        Log.e("Nequi", "error en nequi: $response")
                    }
                }
            })
        }


    private fun crearSolicitudPost(accessToken: String) {
        val nequiPaymentManager=NequiPaymentManager(accessToken)
        nequiPaymentManager.sendPaymentWithPush(
            "3213462822",
        Utilidades.generarIdUnico(),
        "100000",
        "ixi",
        "prueba2",
        "prueba3"
        ) { success, response ->
            if (success) {
                Log.d("Nequi", "Respuesta del servidor: $response")
            } else {
                Log.e("Nequi", "error en nequi: $response")
            }
        }
    }

    private fun mostrarSaldo() {
        if(DriverProvider.datosConductor?.saldo!=null) binding.saldoActual.text=DriverProvider.datosConductor?.saldo.toString().formatoMonenda()
    }

    fun otrometodo(){

//        val factory = ApiClientFactory()
//        factory.apiKey("vG7JvMu6laaBWviQIsrJaaKaeWNRHaX44dFtDfBr")
//
//// Se instancia un nuevo cliente
//        val apiClient = factory.build(NequiPaymentsGatewayClient::class.java)
//
//// Se instancia la clase para autenticarse
//        val auth = NequiAuth.getInstance().fromEnvVars()
//
//        val phoneNumber = 123L
//        val value = 100
//        val code = "12345"
//        val reference1 = "reference1"
//        val reference2 = "reference2"
//        val reference3 = "reference3"
//
//// Consumo del servicio, retorna un JsonObject con la respuesta del servicio
//        val response = apiClient.servicesPaymentserviceUnregisteredpaymentPost(
//            BodyUtils.getBodyUnregisteredPayment(
//                phoneNumber,
//                value,
//                code,
//                reference1,
//                reference2,
//                reference3
//            ),
//            auth.getToken()
//        )
//        Log.d("Nequi","respuesta nueva: $response")
    }
}