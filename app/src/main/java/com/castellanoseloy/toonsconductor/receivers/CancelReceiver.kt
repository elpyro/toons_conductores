package com.castellanoseloy.toonsconductor.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.castellanoseloy.toonsconductor.activitys.mapActivity.MapaActivity
import com.castellanoseloy.toonsconductor.proveedor.BookingProvider


class CancelReceiver: BroadcastReceiver() {

    val bookingProvider = BookingProvider()

    override fun onReceive(context: Context, intent: Intent) {

        val idBooking = intent.extras?.getString("idBooking")
        cancelBooking(idBooking!!)
    }

    private fun cancelBooking(idBooking: String) {

        bookingProvider.getBooking().get().addOnCompleteListener { task->
            if(task.isSuccessful){
                val document = task.result

                if (document.exists()) {
                    bookingProvider.updateStatus(idBooking, "cancel").addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FIRESTORE", "booking actualizado a cancelado")
                        }
                    }
                }
            }
        }
    }

}