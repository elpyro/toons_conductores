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
    val tiempoDescanso:Long=60000

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
                val responseBody = responseMessage?.optJSONObject("ResponseBody")
                val anyObject = responseBody?.optJSONObject("any")
                val getStatusPaymentRS = anyObject?.optJSONObject("getStatusPaymentRS")
                Log.d("Pruebas Nequi", "RESPUESTA EXITOSA: $responseBodyString")

                return getStatusPaymentRS?.optString("status")!!
            }

}