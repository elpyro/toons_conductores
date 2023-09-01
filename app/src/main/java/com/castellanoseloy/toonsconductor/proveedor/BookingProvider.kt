package com.castellanoseloy.toonsconductor.proveedor

import android.util.Log
import com.castellanoseloy.toonsconductor.modelos.Booking
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BookingProvider {

    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Bookings")
    private val authProvider = Autenticacion()

    fun getBooking(): com.google.firebase.database.Query {
        // Para obtener reservas del conductor actual (por idDriver)
        return db.orderByChild("idDriver").equalTo(authProvider.obtenerId())
    }

    fun updateStatus(bookingId: String, status: String): Task<Void> {
        // Para actualizar el estado de una reserva
        val bookingUpdateMap = HashMap<String, Any>()
        bookingUpdateMap["status"] = status

        return db.child(bookingId).updateChildren(bookingUpdateMap)
            .addOnFailureListener { exception ->
                Log.d("REALTIME DATABASE", "ERROR: ${exception.message}")
            }
    }

}