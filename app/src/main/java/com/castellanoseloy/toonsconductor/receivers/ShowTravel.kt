package com.castellanoseloy.toonsconductor.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.castellanoseloy.toonscliente.objetos.Utilidades
import com.castellanoseloy.toonsconductor.activitys.mapTripActivity.MapTripActivity
import com.castellanoseloy.toonsconductor.modelos.Booking
import com.castellanoseloy.toonsconductor.proveedor.BookingAceptedsProvider
import com.castellanoseloy.toonsconductor.proveedor.BookingProvider


class ShowTravel: BroadcastReceiver() {

    val bookingProvider = BookingProvider()

    override fun onReceive(context: Context, intent: Intent) {

        val idBooking = intent.extras?.getString("idBooking")
        showBooking(context, idBooking!!)
    }

    private fun showBooking(context: Context, idBooking: String) {
       val bookingAceptedsProvider = BookingAceptedsProvider()
        bookingProvider.getBooking().get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result

                if (document.exists()) {
                    val booking = document.getValue(Booking::class.java)
                    bookingProvider.updateStatus(idBooking, "accept").addOnCompleteListener {

                        if (it.isSuccessful) {
                            bookingAceptedsProvider.create(booking!!).addOnCompleteListener {
                                Utilidades.mostrarToast("Viaje Aceptado", context)
                                inciarViaje(context)

                            }
                        } else {

                        }
                    }
                } else {
                    // El booking no existe

                }
            } else {

            }
        }
    }

    private fun inciarViaje(context: Context) {
        val i = Intent(context, MapTripActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        i.action = Intent.ACTION_RUN
        context.startActivity(i)
    }

    private fun acceptBooking(context: Context, idBooking: String) {
        bookingProvider.updateStatus(idBooking, "accept").addOnCompleteListener {

            if (it.isSuccessful) {
                goToMapTrip(context)
            }
            else {
                Log.d("RECEIVER", "NO SE PUDO ACTUALIZAR EL ESTADO DEL VIAJE")
            }
        }
    }

    private fun goToMapTrip(context: Context) {
        val i = Intent(context, MapTripActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        i.action = Intent.ACTION_RUN
        context.startActivity(i)
    }

}