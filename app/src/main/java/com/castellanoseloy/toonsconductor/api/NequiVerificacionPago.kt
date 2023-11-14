package com.castellanoseloy.toonsconductor.api

import android.util.Log
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonscliente.objetos.Utilidades.generarMessageID
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject

class NequiVerificacionPago {
    val url = "https://api.sandbox.nequi.com/payments/v2/-services-paymentservice-getstatuspayment"
    var nequiOAuth2Manager=NequiOAuth2Manager()
    val tiempoDescanso:Long=360000 //6 minutos

    fun verificarEstadoDePago(
        codigoMensajeVerificacion: String,
        estado: (String) -> Unit
    ) {
        nequiOAuth2Manager.getAccessToken {accessToken->

            if (accessToken != null) {

                val apiKey = nequiOAuth2Manager.apiKey
                val fecha = Utilidades.obtenerFechaActualEnFormatoISO8601()
                val mensajeId = generarMessageID()
                val jsonBody = """
{
  "RequestMessage": {
    "RequestHeader": {
      "Channel": "PNP04-C001",
      "RequestDate": "$fecha",
      "MessageID": "$mensajeId",
      "ClientID": "${nequiOAuth2Manager.clientId}",
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
          "codeQR": "$codigoMensajeVerificacion"
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

                var status = "33" //es el codigo de nequi que representa PAGO PENDIENTE
                while (status == "33") {
                    val response = client.newCall(request).execute()
                    Log.d("Pruebas Nequi", "jsonBody:  $jsonBody")

                    if (response.isSuccessful) {
                        status = obtenerStatusCode(response!!)

                        Log.d("Pruebas Nequi", "Codigo Estado:$status")

                        estado(status) // Call the callback function with the current status

                    } else {
                        Log.e("Pruebas Nequi", "Error en la respuesta ${response.message}")
                        estado("error") // Call the callback function with an error message
                    }

                    Thread.sleep(tiempoDescanso)
                }
            }
        }
    }

            fun obtenerStatusCode(responseBody: Response): String {
                val responseBodyString = responseBody.body?.string()
                val jsonResponse = JSONObject(responseBodyString)
                val responseMessage = jsonResponse.optJSONObject("ResponseMessage")

                // Verificar si ResponseMessage es nulo
                if (responseMessage != null) {
                    val responseBody = responseMessage.optJSONObject("ResponseBody")

                    // Verificar si ResponseBody es nulo
                    if (responseBody != null) {
                        val anyObject = responseBody.optJSONObject("any")

                        // Verificar si 'any' es nulo
                        if (anyObject != null) {
                            val getStatusPaymentRS = anyObject.optJSONObject("getStatusPaymentRS")

                            // Verificar si 'getStatusPaymentRS' es nulo
                            if (getStatusPaymentRS != null) {
                                val status = getStatusPaymentRS.optString("status", "ValorPredeterminadoSiEsNull")

                                // Regresar el valor del estado
                                return status
                            }
                        }
                    }
                }

                // Si no se puede obtener el estado, puedes regresar un valor predeterminado o lanzar una excepción según tu lógica de manejo de errores.
                return "34"
            }

}