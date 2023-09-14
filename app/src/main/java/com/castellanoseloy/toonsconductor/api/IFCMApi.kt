package com.castellanoseloy.toonsconductor.api


import com.castellanoseloy.toonsconductor.modelos.FCMBody
import com.castellanoseloy.toonsconductor.modelos.FCMResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMApi {

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAzGYCaNg:APA91bGRtwFsVpdAOXnOP7EJShWqNMbV64udKRWi8SWvCxzwu48ckC8BHXFz5am7A4JIWgVQ2O_5WY5AYhRn82yij64vIW9w-uqTcyydzF0OdSAMZFgU_JHYy8GAe5ubPSr0whVeQEHY"
    )
    @POST("fcm/send")
    fun send(@Body body: FCMBody): Call<FCMResponse>


}