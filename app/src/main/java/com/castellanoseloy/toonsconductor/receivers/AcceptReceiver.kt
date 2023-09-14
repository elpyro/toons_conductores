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
import com.castellanoseloy.toonsconductor.proveedor.CarProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot



class AcceptReceiver: BroadcastReceiver() {

    val bookingProvider = BookingProvider()
    val bookingAceptedsProvider = BookingAceptedsProvider()

    override fun onReceive(context: Context, intent: Intent) {
        val idBooking = intent.extras?.getString("idBooking")
        acceptBooking(context, idBooking!!)
    }

    private fun acceptBooking(context: Context, idBooking: String) {
        Log.d("REALTIME DATABASE", "ACEPTANDO PARA: $idBooking")
        var bookingRef = bookingProvider.getBooking()
        bookingRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (childSnapshot in snapshot.children) {

                    val booking = childSnapshot.getValue(Booking::class.java)
                    Log.d("REALTIME DATABASE", "BOOKING: $booking")

                if (booking != null) {
                    bookingProvider.updateStatus(idBooking, "accept").addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("REALTIME DATABASE", "VIAJE CREADO: " + booking)
                            booking!!.status = "accept" // Crear el booking aceptado
                            booking.car= CarProvider.datosCarroEnUso //establecer el carro en uso
                            bookingAceptedsProvider.create(booking).addOnCompleteListener {
                                Utilidades.mostrarToast("Viaje Aceptado", context)
                                goToMapTrip(context)
                            }
                        }
                    }
                }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Este método se llama si ocurre un error al recuperar los datos
                Log.d("REALTIME DATABASE", "Error: ${error.message}")
            }
        })

    }

    private fun goToMapTrip(context: Context) {
        val i = Intent(context, MapTripActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        i.action = Intent.ACTION_RUN
        context.startActivity(i)
    }

}