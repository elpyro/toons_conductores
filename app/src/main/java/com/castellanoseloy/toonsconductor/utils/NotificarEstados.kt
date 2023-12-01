package com.castellanoseloy.toonsconductor.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.castellanoseloy.toonsconductor.modelos.FCMBody
import com.castellanoseloy.toonsconductor.modelos.FCMResponse
import com.castellanoseloy.toonsconductor.proveedor.NotificationProvider
import com.optic.uberclonedriverkotlin.models.Client
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificarEstados {
    private val notificationProvider = NotificationProvider()

    fun sendNotification(status: String, client: Client, context: Context) {

        if(client.token==null) return

        val map = HashMap<String, String>()
        map.put("title", "Estado del viaje")
        map.put("body", status)

        val body = FCMBody(
            to = client?.token!!,
            priority = "high",
            ttl = "4500s",
            data = map
        )

        notificationProvider.sendNotification(body).enqueue(object: Callback<FCMResponse> {
            override fun onResponse(call: Call<FCMResponse>, response: Response<FCMResponse>) {
                if (response.body() != null) {
                    Log.d("NOTIFICATION", "Se ha enviado la notificacion")
                }
                else {
                    Toast.makeText(context, "No se pudo enviar la notificacion", Toast.LENGTH_LONG).show()
                    Log.e("NOTIFICATION", "ERROR en notificacion")
                }
            }

            override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                Toast.makeText(context, "No se pudo enviar la notificacion", Toast.LENGTH_LONG).show()
                Log.e("NOTIFICATION", "ERROR: ${t.message}")
            }

        })
    }
}