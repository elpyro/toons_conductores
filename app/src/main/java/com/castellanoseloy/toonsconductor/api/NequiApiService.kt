package com.castellanoseloy.toonsconductor.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


interface NequiApiService {
    @POST("/payments/v2/-services-paymentservice-unregisteredpayment")

    fun realizarPago(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String, // Agregado para el apikey
        @Body request: RequestBody
    ): Call<ResponseBody>
}
