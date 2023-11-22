package com.castellanoseloy.toonsconductor.proveedor

import android.util.Log
import com.castellanoseloy.toonsconductor.modelos.BookingModel
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query

import com.optic.uberclonedriverkotlin.providers.DriverProvider

class BookingAceptedsProvider {

    private val db = FirebaseDatabase.getInstance().reference.child("BookingsAccepteds")


    fun create(booking: BookingModel): Task<Void> {
        return db.child(DriverProvider.datosConductor?.id!!).setValue(booking).addOnFailureListener {
            Log.d("REALTIME_DB", "ERROR: ${it.message}")
        }
    }

    fun getBookingAccepteds(): Query {
        return db.orderByChild("idDriver").equalTo(DriverProvider.datosConductor?.id).limitToFirst(1)
    }

    fun updateStatus(idDriver: String, status: String): Task<Void> {
        return db.child(idDriver).child("status").setValue(status).addOnFailureListener { exception ->
            Log.d("REALTIME_DB", "ERROR: ${exception.message}")
        }
    }

    fun deleteBookingsForDriver(idDriver: String): Task<Void> {
        // Obtén la referencia a la ubicación de los datos que deseas eliminar
        val driverRef = db.child(idDriver)

        // Realiza la eliminación de los datos en la ubicación especificada
        return driverRef.removeValue().addOnFailureListener { exception ->
            Log.d("REALTIME_DB", "ERROR: ${exception.message}")
        }
    }

}