package com.castellanoseloy.toonsconductor.proveedor


import com.castellanoseloy.toonsconductor.api.IFCMApi
import com.castellanoseloy.toonsconductor.api.RetrofitClient
import com.castellanoseloy.toonsconductor.modelos.FCMBody
import com.castellanoseloy.toonsconductor.modelos.FCMResponse

import retrofit2.Call

class NotificationProvider {

    private val URL = "https://fcm.googleapis.com"

    fun sendNotification(body: FCMBody): Call<FCMResponse> {
        return RetrofitClient.getClient(URL).create(IFCMApi::class.java).send(body)
    }

}