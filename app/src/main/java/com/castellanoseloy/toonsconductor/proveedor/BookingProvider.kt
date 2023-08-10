package com.castellanoseloy.toonsconductor.proveedor

import android.util.Log
import com.castellanoseloy.toonsconductor.modelos.Booking
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BookingProvider {

    val db = Firebase.firestore.collection("Bookings")
    val authProvider = Autenticacion()


    fun create(booking: Booking): Task<Void> {
        return db.document(authProvider.obtenerId()).set(booking).addOnFailureListener {
            Log.d("FIRESTORE", "ERROR: ${it.message}")
        }
    }

    fun getBooking(): Query {
        return db.whereEqualTo("idDriver",authProvider.obtenerId())
    }

    fun updateStatus(idClient: String, status: String): Task<Void> {
        return db.document(idClient).update("status", status).addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }

}