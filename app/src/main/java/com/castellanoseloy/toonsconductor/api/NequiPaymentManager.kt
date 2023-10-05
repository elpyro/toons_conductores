package com.castellanoseloy.toonsconductor.api

import android.util.Log
import com.castellanoseloy.toonscliente.objetos.Utilidades
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class NequiPaymentManager(private val accessToken: String) {

    private val client: OkHttpClient

    init {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    fun sendPaymentWithPush(
        phoneNumber: String,
        code: String,
        value: String,
        reference1: String,
        reference2: String,
        reference3: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val requestBody = JSONObject().apply {
            put("RequestMessage", JSONObject().apply {
                put("RequestHeader", JSONObject().apply {
                    put("Channel", "PNP04-C001")
                    put("RequestDate", getCurrentDateTimeInISO8601())
                    put("MessageID", Utilidades.generarIdUnico())
                    put("ClientID", "1fjejjqsiabk93sdaf1255ef0q")
                    put("Destination", JSONObject().apply {
                        put("ServiceName", "PaymentsService")
                        put("ServiceOperation", "unregisteredPayment")
                        put("ServiceRegion", "C001")
                        put("ServiceVersion", "1.2.0")
                    })
                })
                put("RequestBody", JSONObject().apply {
                    put("any", JSONObject().apply {
                        put("unregisteredPaymentRQ", JSONObject().apply {
                            put("phoneNumber", phoneNumber)
                            put("code", code)
                            put("value", value)
                            put("reference1", reference1)
                            put("reference2", reference2)
                            put("reference3", reference3)
                        })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url("https://api.sandbox.nequi.com/payments/v2/-services-paymentservice-unregisteredpayment")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("x-api-key", "vG7JvMu6laaBWviQIsrJaaKaeWNRHaX44dFtDfBr")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("application/json".toMediaType(), requestBody.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, null)
                Log.e("Nequi", "fallo la respuesta")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    Log.d("Nequi", "La respuesta: $response")
                    callback(true, responseBody)
                } else {
                    Log.e("Nequi", "error en nequi: $response")
                    callback(false, responseBody)
                }
            }
        })
    }

    fun getCurrentDateTimeInISO8601(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC") // Configura la zona horaria a UTC
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }
}
