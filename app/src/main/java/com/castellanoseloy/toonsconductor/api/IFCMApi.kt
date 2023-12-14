package com.castellanoseloy.toonsconductor.api


import com.castellanoseloy.toonsconductor.modelos.FCMBody
import com.castellanoseloy.toonsconductor.modelos.FCMResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMApi {
    //configura la apikey de mensajes push notificacion
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAvM3ghZE:APA91bFiDmFeTQ0b-ij49lpPQphxWYug3pEJ0KKYCCNNga9Sm1fGzrfb42HSw-omnydsF4yO91sYORoB75XkBc3D5fbnoR_QyJp7A4nFtXYWr65hH1zSlZZaBwLoQtuTzYPzRv23efUT"
    )
    @POST("fcm/send")
    fun send(@Body body: FCMBody): Call<FCMResponse>


}