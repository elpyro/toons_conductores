package com.castellanoseloy.toonsconductor.api

import android.util.Log
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class NequiOAuth2Manager {

    private val client: OkHttpClient
    var clientId= "1fjejjqsiabk93sdaf1255ef0q"
    var clientSecret="15scd07u4tk051pfspnddj9pl4uh14rtaka5glt7prq3pff2bd0r"

    init {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    fun getAccessToken( callback: (String?) -> Unit) {
        val requestBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .build()

        val request = Request.Builder()
            .url("https://oauth.sandbox.nequi.com/oauth2/token")
            .addHeader("Authorization", "Basic ${encodeCredentials(clientId, clientSecret)}")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null) // Devuelve nulo en caso de error
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {

                    val responseBody = response.body?.string()
                    val accessToken = parseAccessToken(responseBody)
                    Log.d("Nequi", "Token obtenido: $accessToken")
                    callback(accessToken) // Devuelve el token obtenido
                } else {
                    Log.e("Nequi", "Error obteniendo el token")
                    callback(null) // Devuelve nulo en caso de error de autenticación
                }
            }
        })
    }

    private fun encodeCredentials(clientId: String, clientSecret: String): String {
        val credentials = "$clientId:$clientSecret"
        return android.util.Base64.encodeToString(credentials.toByteArray(), android.util.Base64.NO_WRAP)
    }

    private fun parseAccessToken(responseBody: String?): String? {
        try {
            val jsonObject = JSONObject(responseBody)
            return jsonObject.optString("access_token")
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }
}
