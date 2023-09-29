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

        val map = HashMap<String, String>()
        map.put("title", "ESTADO DEL VIAJE")
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

                    if (response.body()!!.success == 1) {
                        Toast.makeText(context, "Se envio la notificacion a: "+client?.nombre+" "+client?.apellido!!, Toast.LENGTH_LONG).show()
                    }
                    else {
                        Toast.makeText(context, "No se pudo enviar la notificacion", Toast.LENGTH_LONG).show()
                    }

                }
                else {
                    Toast.makeText(context, "No se pudo enviar la notificacion", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                Log.d("NOTIFICATION", "ERROR: ${t.message}")
            }

        })
    }
}