package com.castellanoseloy.toonsconductor.api
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException

class NequiPaymentCancellationManager(private val accessToken: String) {

    private val client: OkHttpClient

    init {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    fun cancelPaymentNotification(
        code: String,
        phoneNumber: String,
        transactionId: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val requestBody = JSONObject().apply {
            put("RequestMessage", JSONObject().apply {
                put("RequestHeader", JSONObject().apply {
                    put("Channel", "PNP04-C001")
                    put("RequestDate", "2017-06-21T20:26:12.654Z")
                    put("MessageID", "1234567890")
                    put("ClientID", "12345")
                    put("Destination", JSONObject().apply {
                        put("ServiceName", "PaymentsService")
                        put("ServiceOperation", "cancelUnregisteredPayment")
                        put("ServiceRegion", "C001")
                        put("ServiceVersion", "1.0.0")
                    })
                })
                put("RequestBody", JSONObject().apply {
                    put("any", JSONObject().apply {
                        put("cancelUnregisteredPaymentRQ", JSONObject().apply {
                            put("code", code)
                            put("phoneNumber", phoneNumber)
                            put("transactionId", transactionId)
                        })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url("https://api.nequi.com/-services-paymentservice-cancelunregisteredpayment")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("x-api-key", "vG7JvMu6laaBWviQIsrJaaKaeWNRHaX44dFtDfBr")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("application/json".toMediaType(), requestBody.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, null)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    Log.d("Nequi", "respuesta para  cancelar: $response")
                    callback(true, responseBody)
                } else {
                    Log.e("Nequi", "fallo la respuesta")
                    callback(false, responseBody)
                }
            }
        })
    }
}
