package com.castellanoseloy.toonsconductor.apiimport

import android.util.Log
import com.castellanoseloy.toonscliente.objetos.Utilidades
import okhttp3.MediaType.Companion.toMediaType


import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class NequiPaymentStatusManager(private val accessToken: String) {

    private val client: OkHttpClient

    init {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    fun checkPaymentStatus(trnId: String, callback: (Boolean, String?) -> Unit) {
        val requestBody = JSONObject().apply {
            put("RequestMessage", JSONObject().apply {
                put("RequestHeader", JSONObject().apply {
                    put("Channel", "PNP04-C001")
                    put("RequestDate", "2017-06-21T20:26:12.654Z")
                    put("MessageID", Utilidades.generarIdUnico())
                    put("ClientID", "12345")
                    put("Destination", JSONObject().apply {
                        put("ServiceName", "PaymentsService")
                        put("ServiceOperation", "getStatusPayment")
                        put("ServiceRegion", "C001")
                        put("ServiceVersion", "1.0.0")
                    })
                })
                put("RequestBody", JSONObject().apply {
                    put("any", JSONObject().apply {
                        put("getStatusPaymentRQ", JSONObject().apply {
                            put("trnId", trnId)
                        })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url("https://api.nequi.com/-services-paymentservice-getstatuspayment")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("x-api-key", "vG7JvMu6laaBWviQIsrJaaKaeWNRHaX44dFtDfBr")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("application/json".toMediaType(), requestBody.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, null)
                Log.e("Nequi", "Fallo la respuesta")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    Log.d("Nequi", "La respuesta: $response")
                    callback(true, responseBody)
                } else {
                    Log.e("Nequi", "Error en nequi: $response")
                    callback(false, responseBody)
                }
            }
        })
    }
}
